package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.PaymentIntent;
import org.example.domain.enums.CryptoNetwork;
import org.example.dto.blockchain.EscrowDealBlockchainData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.Optional;
import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockchainEscrowService {

    public void verifyPaymentTransactionReceipt(PaymentIntent paymentIntent) {
        CryptoNetwork network = paymentIntent.getPaymentMethod().getNetwork();

        if (network != CryptoNetwork.HARDHAT_LOCAL) {
            throw new IllegalArgumentException("Unsupported blockchain network for receipt verification: " + network);
        }

        try {
            Web3j web3j = Web3j.build(new HttpService(hardhatLocalRpcUrl));

            Optional<TransactionReceipt> optionalReceipt = web3j
                    .ethGetTransactionReceipt(paymentIntent.getPaymentTxHash())
                    .send()
                    .getTransactionReceipt();

            if (optionalReceipt.isEmpty()) {
                throw new IllegalArgumentException("Payment transaction receipt not found");
            }

            TransactionReceipt receipt = optionalReceipt.get();

            if (!receipt.isStatusOK()) {
                throw new IllegalArgumentException("Payment transaction failed on blockchain");
            }

            String txTo = receipt.getTo();

            if (txTo == null || !normalizeAddress(txTo).equals(normalizeAddress(paymentIntent.getEscrowContract()))) {
                throw new IllegalArgumentException("Payment transaction was not sent to escrow contract");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to verify payment transaction receipt", e);
        }
    }

    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }

        return address.trim().toLowerCase();
    }

    @Value("${blockchain.hardhatLocalRpcUrl}")
    private String hardhatLocalRpcUrl;

    public EscrowDealBlockchainData getDeal(PaymentIntent paymentIntent) {
        CryptoNetwork network = paymentIntent.getPaymentMethod().getNetwork();

        if (network != CryptoNetwork.HARDHAT_LOCAL) {
            throw new IllegalArgumentException("Unsupported blockchain network for verification: " + network);
        }

        try {
            Web3j web3j = Web3j.build(new HttpService(hardhatLocalRpcUrl));

            Function function = new Function(
                    "deals",
                    List.of(new Uint256(new BigInteger(paymentIntent.getEscrowDealId()))),
                    List.of(
                            new TypeReference<Uint256>() {},
                            new TypeReference<Address>() {},
                            new TypeReference<Address>() {},
                            new TypeReference<Address>() {},
                            new TypeReference<Uint256>() {},
                            new TypeReference<Uint8>() {},
                            new TypeReference<Uint256>() {},
                            new TypeReference<Uint256>() {},
                            new TypeReference<Uint256>() {}
                    )
            );

            String encodedFunction = FunctionEncoder.encode(function);

            String value = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                            null,
                            paymentIntent.getEscrowContract(),
                            encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
            ).send().getValue();

            var decoded = FunctionReturnDecoder.decode(value, function.getOutputParameters());

            if (decoded.size() != 9) {
                throw new IllegalStateException("Invalid escrow deal response from blockchain");
            }

            return new EscrowDealBlockchainData(
                    (BigInteger) decoded.get(0).getValue(),
                    decoded.get(1).getValue().toString(),
                    decoded.get(2).getValue().toString(),
                    decoded.get(3).getValue().toString(),
                    (BigInteger) decoded.get(4).getValue(),
                    (BigInteger) decoded.get(5).getValue(),
                    (BigInteger) decoded.get(6).getValue(),
                    (BigInteger) decoded.get(7).getValue(),
                    (BigInteger) decoded.get(8).getValue()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read escrow deal from blockchain", e);
        }
    }
}
import { network } from "hardhat";

async function main() {
  const connection = await network.connect();
  const ethers = connection.ethers;

  const [buyer, seller] = await ethers.getSigners();

  const tokenAddress = process.env.TOKEN_CONTRACT;
  const escrowAddress = process.env.ESCROW_CONTRACT;
  const amountAtomic = process.env.AMOUNT_ATOMIC;

  if (!tokenAddress) {
    throw new Error("TOKEN_CONTRACT env is required");
  }
  if (!escrowAddress) {
    throw new Error("ESCROW_CONTRACT env is required");
  }
  if (!amountAtomic) {
    throw new Error("AMOUNT_ATOMIC env is required");
  }

  const amount = BigInt(amountAtomic);

  console.log("Buyer: ", buyer.address);
  console.log("Seller:", seller.address);
  console.log("Token: ", tokenAddress);
  console.log("Escrow:", escrowAddress);
  console.log("Amount (atomic):", amount.toString());

  const token = await ethers.getContractAt("MockERC20", tokenAddress) as any;
  const escrow = await ethers.getContractAt("DragonDealEscrow", escrowAddress) as any;

  // 1. Mint tokens to buyer
  console.log("\nMinting tokens to buyer...");
  const mintTx = await token.mint(buyer.address, amount);
  await mintTx.wait();

  const buyerBalance = await token.balanceOf(buyer.address);
  console.log("Buyer balance:", buyerBalance.toString());

  // 2. Approve escrow to spend tokens
  console.log("\nApproving escrow...");
  const approveTx = await token.connect(buyer).approve(escrowAddress, amount);
  await approveTx.wait();

  const allowance = await token.allowance(buyer.address, escrowAddress);
  console.log("Allowance:", allowance.toString());

  // 3. createDeal
  console.log("\nCreating escrow deal...");
  const createTx = await escrow.connect(buyer).createDeal(
    seller.address,
    tokenAddress,
    amount
  );
  const createReceipt = await createTx.wait();

  // Parse DealCreated event for dealId
  let escrowDealId: bigint | null = null;
  for (const log of createReceipt!.logs) {
    try {
      const parsed = escrow.interface.parseLog(log as any);
      if (parsed && parsed.name === "DealCreated") {
        escrowDealId = parsed.args.dealId;
        break;
      }
    } catch {
      // skip unrelated logs
    }
  }

  if (escrowDealId === null) {
    throw new Error("DealCreated event not found in tx receipt");
  }

  console.log("Escrow deal id:", escrowDealId.toString());

  // 4. payDeal
  console.log("\nPaying escrow deal...");
  const payTx = await escrow.connect(buyer).payDeal(escrowDealId);
  const payReceipt = await payTx.wait();

  const paymentTxHash = payReceipt!.hash;
  console.log("Payment tx hash:", paymentTxHash);

  // Verify deal status on-chain
  const deal = await escrow.getDeal(escrowDealId);
  console.log("\nDeal status on-chain:", deal.status.toString());

  console.log("");
  console.log("ESCROW_DEAL_ID=" + escrowDealId.toString());
  console.log("PAYMENT_TX_HASH=" + paymentTxHash);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});

import { network } from "hardhat";

async function main() {
    const connection = await network.connect();
    const ethers = connection.ethers;

    const signers = await ethers.getSigners();
    const buyer = signers[1];
    const seller = signers[2];

    const escrowAddress = process.env.ESCROW_CONTRACT;
    const escrowDealId = process.env.ESCROW_DEAL_ID;

    if (!escrowAddress) {
        throw new Error("ESCROW_CONTRACT is required");
    }

    if (!escrowDealId) {
        throw new Error("ESCROW_DEAL_ID is required");
    }

    const dealId = BigInt(escrowDealId);

    console.log("Buyer:", buyer.address);
    console.log("Seller:", seller.address);
    console.log("Escrow:", escrowAddress);
    console.log("Escrow deal id:", dealId.toString());

    const escrow = await ethers.getContractAt("DragonDealEscrow", escrowAddress) as any;

    const dealBefore = await escrow.getDeal(dealId);

    console.log("");
    console.log("DEAL BEFORE:");
    console.log("buyer:", dealBefore.buyer);
    console.log("seller:", dealBefore.seller);
    console.log("token:", dealBefore.token);
    console.log("amount:", dealBefore.amount.toString());
    console.log("status:", dealBefore.status.toString());

    if (dealBefore.status.toString() !== "2") {
        throw new Error("Deal is not PAID");
    }

    console.log("");
    console.log("Releasing escrow deal...");

    const releaseTx = await escrow.connect(buyer).releaseDeal(dealId);
    const releaseReceipt = await releaseTx.wait();

    const dealAfter = await escrow.getDeal(dealId);

    console.log("");
    console.log("DEAL AFTER:");
    console.log("status:", dealAfter.status.toString());

    console.log("");
    console.log("BACKEND UPDATE:");
    console.log(`RELEASE_TX_HASH=${releaseReceipt!.hash}`);
}

main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
});
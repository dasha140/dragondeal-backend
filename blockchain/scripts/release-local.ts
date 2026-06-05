import { network } from "hardhat";

async function main() {
  const connection = await network.connect();
  const ethers = connection.ethers;

  const [buyer] = await ethers.getSigners();

  const escrowAddress = process.env.ESCROW_CONTRACT;
  const escrowDealId = process.env.ESCROW_DEAL_ID;

  if (!escrowAddress) {
    throw new Error("ESCROW_CONTRACT env is required");
  }
  if (!escrowDealId) {
    throw new Error("ESCROW_DEAL_ID env is required");
  }

  const dealId = BigInt(escrowDealId);

  const escrow = await ethers.getContractAt("DragonDealEscrow", escrowAddress) as any;

  // Read deal before release
  const dealBefore = await escrow.getDeal(dealId);
  console.log("Deal id:      ", dealBefore.id.toString());
  console.log("Buyer:        ", dealBefore.buyer);
  console.log("Seller:       ", dealBefore.seller);
  console.log("Token:        ", dealBefore.token);
  console.log("Amount:       ", dealBefore.amount.toString());
  console.log("Status before:", dealBefore.status.toString(), "(0=NONE 1=CREATED 2=PAID 3=RELEASED)");

  if (dealBefore.status.toString() !== "2") {
    throw new Error("Deal must be PAID (status=2) to release, got status=" + dealBefore.status.toString());
  }

  console.log("\nReleasing deal (caller: buyer)...");
  const releaseTx = await escrow.connect(buyer).releaseDeal(dealId);
  const receipt = await releaseTx.wait();

  console.log("Release tx hash:", receipt!.hash);

  // Verify deal status on-chain
  const dealAfter = await escrow.getDeal(dealId);
  console.log("\nStatus after: ", dealAfter.status.toString(), "(3=RELEASED)");

  // Check seller received tokens
  const token = await ethers.getContractAt("MockERC20", dealAfter.token) as any;
  const sellerBalance = await token.balanceOf(dealAfter.seller);
  console.log("Seller balance:", sellerBalance.toString());

  console.log("");
  console.log("RELEASE_TX_HASH=" + receipt!.hash);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});

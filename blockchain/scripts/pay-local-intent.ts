import { network } from "hardhat";

async function main() {
  const connection = await network.connect();
  const ethers = connection.ethers;

  const [, buyer, seller] = await ethers.getSigners();

  const tokenAddress = process.env.TOKEN_CONTRACT;
  const escrowAddress = process.env.ESCROW_CONTRACT;
  const amountAtomic = process.env.AMOUNT_ATOMIC;

  if (!tokenAddress) {
    throw new Error("TOKEN_CONTRACT is required");
  }

  if (!escrowAddress) {
    throw new Error("ESCROW_CONTRACT is required");
  }

  if (!amountAtomic) {
    throw new Error("AMOUNT_ATOMIC is required");
  }

  const amount = BigInt(amountAtomic);

  console.log("Buyer:", buyer.address);
  console.log("Seller:", seller.address);
  console.log("Token:", tokenAddress);
  console.log("Escrow:", escrowAddress);
  console.log("Amount atomic:", amount.toString());

  const token = await ethers.getContractAt("MockERC20", tokenAddress) as any;
  const escrow = await ethers.getContractAt("DragonDealEscrow", escrowAddress) as any;

  console.log("");
  console.log("Minting local USDC to buyer...");

  const mintTx = await token.mint(buyer.address, amount);
  await mintTx.wait();

  const buyerBalance = await token.balanceOf(buyer.address);

  console.log("Buyer token balance:", buyerBalance.toString());

  console.log("");
  console.log("Creating escrow deal...");

  const createDealTx = await escrow.connect(buyer).createDeal(
      seller.address,
      tokenAddress,
      amount
  );

  const createDealReceipt = await createDealTx.wait();

  let escrowDealId: bigint | null = null;

  for (const log of createDealReceipt!.logs) {
    try {
      const parsedLog = escrow.interface.parseLog(log as any);

  if (parsedLog && parsedLog.name === "DealCreated") {
    escrowDealId = parsedLog.args.dealId;
    break;
  }
} catch {
  // skip unrelated logs
}


    }

    if (escrowDealId === null) {
      throw new Error("DealCreated event not found");
    }

    console.log("Escrow deal id:", escrowDealId.toString());

    console.log("");
    console.log("Approving token...");

    const approveTx = await token.connect(buyer).approve(
        escrowAddress,
        amount
    );

    await approveTx.wait();

    console.log("Token approved");

    console.log("");
    console.log("Paying escrow deal...");

    const payTx = await escrow.connect(buyer).payDeal(escrowDealId);
    const payReceipt = await payTx.wait();

    console.log("Payment tx hash:", payReceipt!.hash);

    const deal = await escrow.getDeal(escrowDealId);

    console.log("");
    console.log("DEAL STATUS:", deal.status.toString());

    console.log("");
    console.log("BACKEND UPDATE:");
    console.log(`ESCROW_DEAL_ID=${escrowDealId.toString()}`);
    console.log(`PAYMENT_TX_HASH=${payReceipt!.hash}`);
  }

  main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
  });

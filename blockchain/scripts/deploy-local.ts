import { network } from "hardhat";

async function main() {
  const connection = await network.connect();
  const ethers = connection.ethers;

  const [deployer] = await ethers.getSigners();

  console.log("Deploying from:", deployer.address);

  // Deploy mock USDC (6 decimals)
  const MockToken = await ethers.getContractFactory("MockERC20");
  const usdc = await MockToken.deploy("DragonDeal Local USDC", "dUSDC", 6) as any;
  await usdc.waitForDeployment();
  const usdcAddress = await usdc.getAddress();
  console.log("Mock USDC:", usdcAddress);

  // Deploy mock USDT (6 decimals)
  const usdt = await MockToken.deploy("DragonDeal Local USDT", "dUSDT", 6) as any;
  await usdt.waitForDeployment();
  const usdtAddress = await usdt.getAddress();
  console.log("Mock USDT:", usdtAddress);

  // Deploy escrow contract
  const Escrow = await ethers.getContractFactory("DragonDealEscrow");
  const escrow = await Escrow.deploy() as any;
  await escrow.waitForDeployment();
  const escrowAddress = await escrow.getAddress();
  console.log("Escrow:", escrowAddress);

  // Whitelist both tokens in escrow
  await escrow.setSupportedToken(usdcAddress, true);
  await escrow.setSupportedToken(usdtAddress, true);
  console.log("Supported tokens enabled: USDC, USDT");

  console.log("");
  console.log("BACKEND UPDATE:");
  console.log(`USDC_TOKEN_CONTRACT=${usdcAddress}`);
  console.log(`USDT_TOKEN_CONTRACT=${usdtAddress}`);
  console.log(`ESCROW_CONTRACT=${escrowAddress}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
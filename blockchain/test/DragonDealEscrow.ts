import { expect } from "chai";
import { network } from "hardhat";

describe("DragonDealEscrow", function () {
  async function deployFixture() {
    const { ethers } = await network.connect();

    const [owner, buyer, seller] = await ethers.getSigners();

    const MockToken = await ethers.getContractFactory("MockERC20");
    const token = await MockToken.deploy("Mock USDC", "mUSDC", 6) as any;


    const Escrow = await ethers.getContractFactory("DragonDealEscrow");
    const escrow = await Escrow.deploy() as any;

    await escrow.setSupportedToken(await token.getAddress(), true);
    await token.mint(buyer.address, 1000n * 1_000_000n);

    return {
      owner,
      buyer,
      seller,
      token,
      escrow
    };
  }

  it("should create and pay deal", async function () {
    const { buyer, seller, token, escrow } = await deployFixture();

    const amount = 25n * 1_000_000n;

    await escrow.connect(buyer).createDeal(
      seller.address,
      await token.getAddress(),
      amount
    );

    await token.connect(buyer).approve(await escrow.getAddress(), amount);

    await expect(escrow.connect(buyer).payDeal(1))
      .to.emit(escrow, "DealPaid");

    const deal = await escrow.getDeal(1);

    expect(deal.buyer).to.equal(buyer.address);
    expect(deal.seller).to.equal(seller.address);
    expect(deal.amount).to.equal(amount);
    expect(deal.status).to.equal(2);
  });

  it("should release funds to seller", async function () {
    const { buyer, seller, token, escrow } = await deployFixture();

    const amount = 25n * 1_000_000n;

    await escrow.connect(buyer).createDeal(
      seller.address,
      await token.getAddress(),
      amount
    );

    await token.connect(buyer).approve(await escrow.getAddress(), amount);
    await escrow.connect(buyer).payDeal(1);

    await expect(escrow.connect(buyer).releaseDeal(1))
      .to.emit(escrow, "DealReleased");

    const sellerBalance = await token.balanceOf(seller.address);

    expect(sellerBalance).to.equal(amount);
  });

  it("should not allow buyer to be seller", async function () {
    const { buyer, token, escrow } = await deployFixture();

    const amount = 25n * 1_000_000n;

    await expect(
      escrow.connect(buyer).createDeal(
        buyer.address,
        await token.getAddress(),
        amount
      )
    ).to.be.revertedWith("Buyer cannot be seller");
  });
});

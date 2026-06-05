// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";

contract DragonDealEscrow is Ownable, ReentrancyGuard {
    using SafeERC20 for IERC20;

    enum DealStatus {
        NONE,
        CREATED,
        PAID,
        RELEASED,
        REFUNDED,
        DISPUTED,
        CANCELLED
    }

    struct Deal {
        uint256 id;
        address buyer;
        address seller;
        address token;
        uint256 amount;
        DealStatus status;
        uint256 createdAt;
        uint256 paidAt;
        uint256 completedAt;
    }

    uint256 private nextDealId = 1;

    mapping(uint256 => Deal) public deals;
    mapping(address => bool) public supportedTokens;

    event TokenSupported(address indexed token, bool supported);

    event DealCreated(
        uint256 indexed dealId,
        address indexed buyer,
        address indexed seller,
        address token,
        uint256 amount
    );

    event DealPaid(
        uint256 indexed dealId,
        address indexed buyer,
        address indexed seller,
        address token,
        uint256 amount
    );

    event DealReleased(
        uint256 indexed dealId,
        address indexed seller,
        address token,
        uint256 amount
    );

    event DealRefunded(
        uint256 indexed dealId,
        address indexed buyer,
        address token,
        uint256 amount
    );

    event DealDisputed(uint256 indexed dealId);

    constructor() Ownable(msg.sender) {}

    function setSupportedToken(address token, bool supported) external onlyOwner {
        require(token != address(0), "Invalid token");
        supportedTokens[token] = supported;
        emit TokenSupported(token, supported);
    }

    function createDeal(
        address seller,
        address token,
        uint256 amount
    ) external returns (uint256) {
        require(seller != address(0), "Invalid seller");
        require(seller != msg.sender, "Buyer cannot be seller");
        require(token != address(0), "Invalid token");
        require(supportedTokens[token], "Token not supported");
        require(amount > 0, "Invalid amount");

        uint256 dealId = nextDealId;
        nextDealId++;

        deals[dealId] = Deal({
            id: dealId,
            buyer: msg.sender,
            seller: seller,
            token: token,
            amount: amount,
            status: DealStatus.CREATED,
            createdAt: block.timestamp,
            paidAt: 0,
            completedAt: 0
        });

        emit DealCreated(dealId, msg.sender, seller, token, amount);

        return dealId;
    }

    function payDeal(uint256 dealId) external nonReentrant {
        Deal storage deal = deals[dealId];

        require(deal.status == DealStatus.CREATED, "Deal not payable");
        require(deal.buyer == msg.sender, "Only buyer can pay");

        IERC20(deal.token).safeTransferFrom(
            msg.sender,
            address(this),
            deal.amount
        );

        deal.status = DealStatus.PAID;
        deal.paidAt = block.timestamp;

        emit DealPaid(
            deal.id,
            deal.buyer,
            deal.seller,
            deal.token,
            deal.amount
        );
    }

    function releaseDeal(uint256 dealId) external nonReentrant {
        Deal storage deal = deals[dealId];

        require(deal.status == DealStatus.PAID, "Deal not paid");
        require(deal.buyer == msg.sender, "Only buyer can release");

        deal.status = DealStatus.RELEASED;
        deal.completedAt = block.timestamp;

        IERC20(deal.token).safeTransfer(
            deal.seller,
            deal.amount
        );

        emit DealReleased(
            deal.id,
            deal.seller,
            deal.token,
            deal.amount
        );
    }

    function refundDeal(uint256 dealId) external onlyOwner nonReentrant {
        Deal storage deal = deals[dealId];

        require(
            deal.status == DealStatus.PAID || deal.status == DealStatus.DISPUTED,
            "Deal not refundable"
        );

        deal.status = DealStatus.REFUNDED;
        deal.completedAt = block.timestamp;

        IERC20(deal.token).safeTransfer(
            deal.buyer,
            deal.amount
        );

        emit DealRefunded(
            deal.id,
            deal.buyer,
            deal.token,
            deal.amount
        );
    }

    function markDisputed(uint256 dealId) external {
        Deal storage deal = deals[dealId];

        require(deal.status == DealStatus.PAID, "Deal not paid");
        require(
            msg.sender == deal.buyer || msg.sender == deal.seller,
            "Only deal participants"
        );

        deal.status = DealStatus.DISPUTED;

        emit DealDisputed(deal.id);
    }

    function getDeal(uint256 dealId) external view returns (Deal memory) {
        return deals[dealId];
    }
}

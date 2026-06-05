package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.AppOrder;
import org.example.domain.AppUser;
import org.example.domain.enums.OrderStatus;
import org.example.dto.request.CompleteOrderRequest;
import org.example.dto.request.CreateOrderByCurrentUserRequest;
import org.example.dto.request.MarkOrderPaidRequest;
import org.example.dto.response.OrderResponse;
import org.example.mapper.OrderMapper;
import org.example.service.CurrentUserService;
import org.example.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final CurrentUserService currentUserService;

    @GetMapping
    public List<OrderResponse> findAllOrders() {
        List<AppOrder> orders = orderService.findAllOrders();
        return orderMapper.toResponseList(orders);
    }

    @GetMapping("/{orderId}")
    public OrderResponse findOrderById(@PathVariable Long orderId) {
        AppOrder order = orderService.findOrderById(orderId);
        return orderMapper.toResponse(order);
    }

    @PostMapping
    public OrderResponse createOrder(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateOrderByCurrentUserRequest request
    ) {
        AppUser buyer = currentUserService.getCurrentUser(httpRequest);
        AppOrder order = orderService.createOrderForCurrentUser(buyer, request.listingId());
        return orderMapper.toResponse(order);
    }

    @GetMapping("/buyer/me")
    public List<OrderResponse> findMyBuyerOrders(HttpServletRequest httpRequest) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<AppOrder> orders = orderService.findBuyerOrders(currentUser.getId());
        return orderMapper.toResponseList(orders);
    }

    @GetMapping("/seller/me")
    public List<OrderResponse> findMySellerOrders(HttpServletRequest httpRequest) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<AppOrder> orders = orderService.findSellerOrders(currentUser.getId());
        return orderMapper.toResponseList(orders);
    }

    @GetMapping("/seller/{sellerId}")
    public List<OrderResponse> findSellerOrders(@PathVariable Long sellerId) {
        List<AppOrder> orders = orderService.findSellerOrders(sellerId);
        return orderMapper.toResponseList(orders);
    }

    @GetMapping("/status/{status}")
    public List<OrderResponse> findOrdersByStatus(@PathVariable OrderStatus status) {
        List<AppOrder> orders = orderService.findOrdersByStatus(status);
        return orderMapper.toResponseList(orders);
    }

    @PatchMapping("/{orderId}/paid")
    public void markOrderAsPaid(
            HttpServletRequest httpRequest,
            @PathVariable Long orderId,
            @Valid @RequestBody MarkOrderPaidRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);

        orderService.markOrderAsPaidForCurrentUser(
                currentUser,
                orderId,
                request.paymentTxHash()
        );
    }

    @PatchMapping("/{orderId}/seller-done")
    public void markSellerDone(@PathVariable Long orderId) {
        orderService.markSellerDone(orderId);
    }

    @PatchMapping("/{orderId}/complete")
    public void completeOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CompleteOrderRequest request
    ) {
        orderService.completeOrder(orderId, request.releaseTxHash());
    }

    @PatchMapping("/{orderId}/cancel")
    public void cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
    }
}
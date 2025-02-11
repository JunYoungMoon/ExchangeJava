package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookServiceTest {

    private OrderBookService orderBookService;

    @BeforeEach
    void setUp() {
        orderBookService = new OrderBookService();
    }

    private CoinOrderDTO createOrder(String price, String amount) {
        CoinOrderDTO order = new CoinOrderDTO();
        order.setOrderPrice(new BigDecimal(price));
        order.setQuantity(new BigDecimal(amount));
        return order;
    }

    @Test
    public void testInitializeBuyOrderBook() {
        //given
        String key = "BTC-KRW";

        // when
        orderBookService.initializeBuyOrderBook(key);

        // then
        assertTrue(orderBookService.getBuyOrderBooks().containsKey(key), "키가 포함되어 있어야 한다.");
        TreeMap<BigDecimal, BigDecimal> buyOrderBook = orderBookService.getBuyOrderBooks().get(key);
        assertNotNull(buyOrderBook, "매수 호가는 null일 수 없다.");
        assertTrue(buyOrderBook.isEmpty(), "매수 호가에 값은 비어 있어야 한다.");
        assertEquals(Comparator.reverseOrder(), buyOrderBook.comparator(), "매수 호가 비교는 역순이어야 한다.");
    }

    @Test
    public void testInitializeSellOrderBook() {
        //given
        String key = "BTC-KRW";

        // when
        orderBookService.initializeSellOrderBook(key);

        // then
        assertTrue(orderBookService.getSellOrderBooks().containsKey(key), "키가 포함되어 있어야 한다.");
        TreeMap<BigDecimal, BigDecimal> sellOrderBook = orderBookService.getSellOrderBooks().get(key);
        assertNotNull(sellOrderBook, "매도 호가는 null일 수 없다.");
        assertTrue(sellOrderBook.isEmpty(), "매도 호가안에 값은 비어 있어야 한다.");
        assertNull(sellOrderBook.comparator(), "매도 호가 비교는 정방향순 이어야 한다.");
    }

    @Test
    void testAddBuyOrderBook() {
        // given
        String key = "BTC-KRW";
        orderBookService.initializeBuyOrderBook(key);
        CoinOrderDTO buyOrder1 = createOrder("5000", "1.5");
        CoinOrderDTO buyOrder2 = createOrder("5000", "0.5");
        CoinOrderDTO buyOrder3 = createOrder("4000", "0.5");
        CoinOrderDTO buyOrder4 = createOrder("3000", "0.5");

        // when
        orderBookService.addBuyOrderBook(key, buyOrder1);
        orderBookService.addBuyOrderBook(key, buyOrder2);
        orderBookService.addBuyOrderBook(key, buyOrder3);
        orderBookService.addBuyOrderBook(key, buyOrder4);

        // then
        TreeMap<BigDecimal, BigDecimal> buyOrderBook = orderBookService.getBuyOrderBooks().get(key);
        assertNotNull(buyOrderBook, "매수 호가는 null일 수 없다.");
//        assertEquals(1, buyOrderBook.size(), "매수 호가에는 하나의 주문이 들어가 있어야 한다.");
//        assertEquals(new BigDecimal("1.5"), buyOrderBook.get(new BigDecimal("5000")), "매수 호가 가격이 일치하는 주문이 있어야 한다.");
    }

    @Test
    void testAddSellOrderBook() {
        // given
        String key = "BTC-KRW";
        orderBookService.initializeSellOrderBook(key);
        CoinOrderDTO sellOrder1 = createOrder("5000", "1.5");
        CoinOrderDTO sellOrder2 = createOrder("5000", "0.5");
        CoinOrderDTO sellOrder3 = createOrder("4000", "0.5");
        CoinOrderDTO sellOrder4 = createOrder("3000", "0.5");

        // when
        orderBookService.addSellOrderBook(key, sellOrder1);
        orderBookService.addSellOrderBook(key, sellOrder2);
        orderBookService.addSellOrderBook(key, sellOrder3);
        orderBookService.addSellOrderBook(key, sellOrder4);

        // then
        TreeMap<BigDecimal, BigDecimal> sellOrderBook = orderBookService.getSellOrderBooks().get(key);
        assertNotNull(sellOrderBook, "매도 호가는 null일 수 없다.");
//        assertEquals(1, sellOrderBook.size(), "매도 호가에는 하나의 주문이 들어가 있어야 한다.");
//        assertEquals(new BigDecimal("1.5"), sellOrderBook.get(new BigDecimal("5000")), "매도 호가 가격이 일치하는 주문이 있어야 한다.");
    }
}
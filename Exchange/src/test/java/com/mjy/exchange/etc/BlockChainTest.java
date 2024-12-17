package com.mjy.exchange.etc;

import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

class Block {
    String hash;
    String previousHash;
    String version;
    LocalDateTime time;
    String nonce;

    public Block(String previousHash, String version, LocalDateTime time, String nonce) {
        this.previousHash = previousHash;
        this.version = version;
        this.time = time;
        this.nonce = nonce;
        this.hash = calculateHash();
    }

    private String calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = previousHash + version + time.toString() + nonce;
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);

        }
    }
}

public class BlockChainTest {
    @Test
    void testBlockChain() {
        Map<String, Block> blockchain = new HashMap<>();

        // 첫 번째 블록 (Genesis Block) 추가
        Block genesisBlock = new Block("0", "1.0", LocalDateTime.now(), "nonce1");
        blockchain.put(genesisBlock.hash, genesisBlock);

        // 두 번째 블록 (이전 블록의 해시값을 이전 블록의 hash로 설정)
        Block secondBlock = new Block(genesisBlock.hash, "1.1", LocalDateTime.now(), "nonce2");
        blockchain.put(secondBlock.hash, secondBlock);

        // 세 번째 블록 (두 번째 블록의 해시값을 이전 블록의 hash로 설정)
        Block thirdBlock = new Block(secondBlock.hash, "1.2", LocalDateTime.now(), "nonce3");
        blockchain.put(thirdBlock.hash, thirdBlock);

        // 테스트 출력
        System.out.println("Genesis Block Hash: " + genesisBlock.hash);
        System.out.println("Second Block Previous Hash: " + secondBlock.previousHash);
        System.out.println("Second Block Hash: " + secondBlock.hash);
        System.out.println("Third Block Previous Hash: " + thirdBlock.previousHash);
        System.out.println("Third Block Hash: " + thirdBlock.hash);
    }
}

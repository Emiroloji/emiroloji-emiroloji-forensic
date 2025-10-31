package com.forensic.case.service;

import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class DigitalSignatureService {

    private static final String ALGORITHM = "SHA256withRSA";
    private static final String KEY_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    private KeyPair keyPair;

    public DigitalSignatureService() {
        generateKeyPair();
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyGen.initialize(KEY_SIZE);
            this.keyPair = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Error generating key pair: " + e.getMessage(), e);
        }
    }

    public byte[] signDocument(byte[] documentData) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(keyPair.getPrivate());
            signature.update(documentData);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException("Error signing document: " + e.getMessage(), e);
        }
    }

    public boolean verifySignature(byte[] documentData, byte[] signatureData) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initVerify(keyPair.getPublic());
            signature.update(documentData);
            return signature.verify(signatureData);
        } catch (Exception e) {
            throw new RuntimeException("Error verifying signature: " + e.getMessage(), e);
        }
    }

    public String getPublicKeyAsString() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    public String getPrivateKeyAsString() {
        return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    public PublicKey getPublicKeyFromString(String publicKeyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing public key: " + e.getMessage(), e);
        }
    }

    public PrivateKey getPrivateKeyFromString(String privateKeyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing private key: " + e.getMessage(), e);
        }
    }

    public String generateCertificate() {
        // In a real implementation, you would generate a proper X.509 certificate
        // For now, return a placeholder
        return "CERTIFICATE_PLACEHOLDER_" + System.currentTimeMillis();
    }

    public boolean verifyCertificate(String certificate) {
        // In a real implementation, you would verify the certificate chain
        // For now, return true as a placeholder
        return true;
    }
}

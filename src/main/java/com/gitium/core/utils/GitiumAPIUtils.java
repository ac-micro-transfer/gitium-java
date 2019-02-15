package com.gitium.core.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.gitium.core.error.GitiumException;
import com.gitium.core.model.Balance;
import com.gitium.core.model.Bundle;
import com.gitium.core.model.Transaction;
import com.gitium.core.pow.ICurl;
import com.gitium.core.pow.SpongeFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

public class GitiumAPIUtils {

    /**
     * Generates a new address
     *
     * @param seed     The tryte-encoded seed. It should be noted that this seed is
     *                 not transferred.
     * @param security The secuirty level of private key / seed.
     * @param index    The index to start search from. If the index is provided, the
     *                 generation of the address is not deterministic.
     * @param checksum The adds 9-tryte address checksum
     * @param curl     The curl instance.
     * @return An String with address.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public static String newAddress(String seed, int security, int index, boolean checksum, ICurl curl)
            throws GitiumException {

        if (!InputValidator.isValidSecurityLevel(security)) {
            throw GitiumException.invalidSecurityLevel();
        }

        Signing signing = new Signing(curl);
        final int[] key = signing.key(Converter.trits(seed), index, security);
        final int[] digests = signing.digests(key);
        final int[] addressTrits = signing.address(digests);

        String address = Converter.trytes(addressTrits);

        if (checksum) {
            address = Checksum.addChecksum(address);
        }
        return address;
    }

    /**
     * Finalizes and signs the bundle transactions. Bundle and inputs are assumed
     * correct.
     * 
     * @param seed               The tryte-encoded seed. It should be noted that
     *                           this seed is not transferred.
     * @param inputs
     * @param bundle             The bundle
     * @param signatureFragments
     * @param curl               The curl instance.
     * @return list of transaction trytes in the bundle
     * @throws ArgumentException When the seed is invalid
     */
    public static List<String> signAndReturn(String seed, final int security, List<Balance> balances, Bundle bundle,
            List<String> signatureFragments, ICurl curl) throws GitiumException {

        bundle.finalize(curl);
        bundle.addTrytes(signatureFragments);

        // SIGNING OF INPUTS
        //
        // Here we do the actual signing of the inputs
        // Iterate over all bundle transactions, find the inputs
        // Get the corresponding private key and calculate the signatureFragment
        for (int i = bundle.getTransactions().size() - 1; i >= 0; i--) {
            if (bundle.getTransactions().get(i).getValue() < 0) {
                String thisAddress = bundle.getTransactions().get(i).getAddress();

                // Get the corresponding keyIndex of the address
                int keyIndex = 0;
                int keySecurity = security;
                for (Balance balance : balances) {
                    if (balance.getAddress().equals(thisAddress)) {
                        keyIndex = balance.getIndex();
                    }
                }

                String bundleHash = bundle.getTransactions().get(i).getBundle();

                // Get corresponding private key of address
                int[] key = new Signing(curl).key(Converter.trits(seed), keyIndex, keySecurity);

                // Get the normalized bundle hash
                int[] normalizedBundleHash = bundle.normalizedBundle(bundleHash);

                // for each security level, add signature
                for (int j = 0; j < keySecurity; j++) {

                    int hashPart = j % 3;

                    // Add parts of signature for bundles with same address
                    if (bundle.getTransactions().get(i + j).getAddress().equals(thisAddress)) {
                        // Use 6562 trits starting from j*6561
                        int[] keyFragment = Arrays.copyOfRange(key, 6561 * j, 6561 * (j + 1));

                        // The current part of the bundle hash
                        int[] bundleFragment = Arrays.copyOfRange(normalizedBundleHash, 27 * hashPart,
                                27 * (hashPart + 1));

                        // Calculate the new signature
                        int[] signedFragment = new Signing(curl).signatureFragment(bundleFragment, keyFragment);

                        // Convert signature to trytes and assign it again to this bundle entry
                        bundle.getTransactions().get(i + j).setSignatureFragments(Converter.trytes(signedFragment));
                    } else {
                        throw GitiumException.inconsistentSecurityLevelAndTransactions();
                    }
                }
            }
        }

        List<String> bundleTrytes = new ArrayList<>();

        // Convert all bundle entries into trytes
        for (Transaction tx : bundle.getTransactions()) {
            bundleTrytes.add(tx.toTrytes());
        }
        Collections.reverse(bundleTrytes);
        return bundleTrytes;
    }

    public static String generateBundle(Object... objects) {
        StringBuilder builder = new StringBuilder();
        for (Object obj : objects) {
            if (obj instanceof String) {
                String text = (String) obj;

                text = text.toUpperCase();
                text = StringUtils.replaceChars(text, "012345678", "ABCDEFGHI");
                text = StringUtils.rightPad(text, 81, '9');

                builder.append(text);
            } else if (obj instanceof Long) {
                builder.append(Converter.trytes(Converter.trits((long) obj, 243)));
            } else {
                return "";
            }
        }

        int[] messageTrits = Converter.trits(builder.toString());
        ICurl curl = SpongeFactory.create(SpongeFactory.Mode.KERL);
        curl.reset();
        curl.absorb(messageTrits, 0, messageTrits.length);
        int[] hash = new int[243];
        curl.squeeze(hash, 0, hash.length);
        return Converter.trytes(hash);
    }

    public static String sign(String seed, String bundle, int index, int security) throws GitiumException {
        Signing signing = new Signing();
        int[] key = signing.key(Converter.trits(seed), index, security);
        int[] normalizedBundleHash = new Bundle().normalizedBundle(bundle);
        String sign = "";
        for (int i = 0; i < security; i++) {
            int[] fragment = Arrays.copyOfRange(key, 6561 * i, 6561 * (i + 1));
            int[] bundleFragment = Arrays.copyOfRange(normalizedBundleHash, 27 * i, 27 * (i + 1));
            int[] signedFragment = signing.signatureFragment(bundleFragment, fragment);
            sign += Converter.trytes(signedFragment);
        }
        return sign;
    }

    public static String formatStandardDate(long millis) {
        return DateFormatUtils.format(millis, "yyyy-MM-dd HH:mm:ss");
    }

    public static String formatContractValue(long value, int decimals) {
        return BigDecimal.valueOf(value, decimals).toPlainString();
    }

    public static long valueOfContractFromString(String src, int decimals) {
        BigDecimal bd = BigDecimal.valueOf(NumberUtils.toDouble(src, 0));
        return bd.movePointRight(decimals).longValue();
    }
}

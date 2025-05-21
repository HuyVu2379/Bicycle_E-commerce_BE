/*
 * @ (#) RandomPhoneNumberGenerator.java       1.0     5/21/2025
 *
 * Copyright (c) 2025. All rights reserved.
 */

package iuh.userservice.services.Impl;
/*
 * @author: Luong Tan Dat
 * @date: 5/21/2025
 */

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomPhoneNumberGenerator {
    private static final String[] PREFIXES = {"090", "091", "092", "093", "094", "095", "096", "097", "098", "099",
                                              "032", "033", "034", "035", "036", "037", "038", "039",
                                              "070", "076", "077", "078", "079",
                                              "081", "082", "083", "084", "085", "086",
                                              "056", "058", "059"};

    private static final Random RANDOM = new Random();

    public static String generatePhoneNumber() {
        String prefix = PREFIXES[RANDOM.nextInt(PREFIXES.length)];
        StringBuilder phoneNumber = new StringBuilder(prefix);

        while (phoneNumber.length() < 10) {
            phoneNumber.append(RANDOM.nextInt(10));
        }

        return phoneNumber.toString();
    }
}

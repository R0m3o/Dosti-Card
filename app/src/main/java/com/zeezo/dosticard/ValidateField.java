package com.zeezo.dosticard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by masho on 02-Feb-17.
 */
public class ValidateField {

    Pattern pattern;
    Matcher matcher;
    String image= "([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)";

    public boolean isValidName(final String name){

        final String name_pattern = "^[A-Z][A-Za-z]{2,10}$";
        pattern = Pattern.compile(name_pattern);
        matcher = pattern.matcher(name);

        return matcher.matches();
    }

    public boolean isValidUsername(final String username){

        final String username_pattern = "^[A-Za-z_][A-Za-z0-9_]{2,15}$";
        pattern = Pattern.compile(username_pattern);
        matcher = pattern.matcher(username);

        return matcher.matches();
    }

    public boolean isValidPassword(final String password){

        final String password_pattern = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20})";
        pattern = Pattern.compile(password_pattern);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    public boolean isValidContactNum(final String contactNo){

        final String contactNo_Pattern = "^(03)[0-9]{9}$";
        pattern = Pattern.compile(contactNo_Pattern);
        matcher = pattern.matcher(contactNo);

        return matcher.matches();
    }

    public boolean isValidAddress(final String address){

        final String address_Pattern = "^[A-Za-z0-9_,.\\x20-]{5,100}$";
        pattern = Pattern.compile(address_Pattern);
        matcher = pattern.matcher(address);

        return matcher.matches();
    }

    public boolean isValidStoreName(final String storeName){

        final String store_name_Pattern = "^[A-Za-z][A-Za-z0-9 ]{2,15}$";
        pattern = Pattern.compile(store_name_Pattern);
        matcher = pattern.matcher(storeName);

        return matcher.matches();
    }

    public boolean isValidStoreBranch(final String storeBranch){

        final String store_branch_Pattern = "^[A-Za-z0-9]{1,15}$";
        pattern = Pattern.compile(store_branch_Pattern);
        matcher = pattern.matcher(storeBranch);

        return matcher.matches();
    }

    public boolean isValidNumbers(final String storePointsOrDiscount){

        final String store_points_Pattern = "^[1-9][0-9]?$|^100$";
        pattern = Pattern.compile(store_points_Pattern);
        matcher = pattern.matcher(storePointsOrDiscount);

        return matcher.matches();
    }

    public boolean isValidPayment(final String payment){

        final String payment_pattern = "^[1-9][0-9]{2,}$";
        pattern = Pattern.compile(payment_pattern);
        matcher = pattern.matcher(payment);

        return matcher.matches();
    }

}

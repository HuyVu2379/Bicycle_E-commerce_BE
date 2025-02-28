package iuh.orderservice.enums;

public enum DiscountType {
    PRODUCT(0), ORDER(1);

    private final int value;

    DiscountType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
package com.cordelta.barr.david;

public class uint32_t {
    private int value;

    public uint32_t value(int value) {
        this.value = value;
        return this;
    }

    public uint32_t value(uint32_t value) {
        return value(value.value);
    }


    public int value() {
        return value;
    }

    public static uint32_t copy(int value) {
        return new uint32_t().value(value);
    }

    public static uint32_t copy(uint32_t value) {
        return new uint32_t().value(value.value);
    }

    public uint32_t copy() {
        return new uint32_t().value(value);
    }

    public static class immutable extends uint32_t {
        public immutable(int value) {
            super.value(value);
        }

        public uint32_t value(int value) {
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof uint32_t)) return false;
        uint32_t uint32_t = (uint32_t) o;
        return value == uint32_t.value;

    }

    @Override
    public int hashCode() {
        return value;
    }
}

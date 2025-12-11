/*
 * Programming Assignment 2
 * CS 211 Spring 2025 (Sections 5-8)
 */
#include <stdio.h>

#include "pa2.h"

/******************************************************************************
 * Provided functions: consult these as examples for how to work with the data
 ******************************************************************************/

/** prints out the entire 128-bit value as an ASCII-encoded hexadecimal string
 *
 *  note: this relies on your value_to_hexdigit() implementation, so if you get
 *  that wrong, this printout will be wrong too!
 */
void print_bv128(struct bv128 val)
{
    char string[32];
    for(int nibble_idx = 31; nibble_idx >= 0; nibble_idx--)
    {
        int nibble = 0;
        if(nibble_idx >= 16)
            nibble = (val.hi >> ((nibble_idx - 16) * 4)) & 0xf;
        else
            nibble = (val.lo >> (nibble_idx * 4)) & 0xf;

        // should never run into this since a nibble should not exceed unsigned value 15
        string[31 - nibble_idx] = is_hexvalue(nibble) ? value_to_hexdigit(nibble) : '?';
    }

    printf("0x%.32s\n", string);
}

/** check if the provided ASCII character is a valid hexadecimal digit */
int is_hexdigit(char c)
{
        /* chars represent their codepoints, and the codepoints can be compared just
        like any other number. refer to the ASCII table for more info */
    return
                (c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'f') ||
                (c >= 'A' && c <= 'F');
}


/******************************************************************************
 * Your Assignment:
 *     Implement the assigned functions declared in pa2.h.
 *     You may NOT:
 *      - Use any compiler-supported 128-bit data types (e.g., __int128_t)
 *      - Use macros or functions from any standard library (e.g., <string.h>, <stdio.h>)
 *      - Use dynamic memory allocation (malloc/free)
 *      - Use static or global variables
 *
 *     Make sure to follow the detailed specifications from pa2.h, including setting
 *     the error conditions correctly where necessary.
 *
 *     Consult the Google Doc for further information on everything.
 ******************************************************************************/

/**
 * This is a complement to is_hexdigit(): checks whether the number v is
 * representable as a single hexadecimal digit (true)
 */
int is_hexvalue(int v)
{
        return (v >= 0 && v <= 15);
}

/**
 * Convert a single ASCII character that encodes a hexadecimal digit to its
 * decimal value (e.g., 'a' -> 10)
 *
 * You could use a switch statement, but there is a much easier way based on the
 * arrangement of ASCII codepoints, which you might figure out by consulting
 * is_hexdigit() above
 */
int hexdigit_to_value(char c) {
    if (c >= '0' && c <= '9') {
        return c - '0';
    }
    if (c >= 'a' && c <= 'f') {
        return c - 'a' + 10;
    }
    if (c >= 'A' && c <= 'F') {
        return c - 'A' + 10;
    }
    return -1; 
}
/**
 * Convert the value of a hexadecimal digit (i.e., 0-15) into its ASCII
 * character representation (e.g., 10 -> 'a')
 *
 * This is the reverse of the previous function, and there is an easier
 * way to do it using ASCII codepoints rather than a giant switch statement.
 */
char value_to_hexdigit(int v) {
    if (!is_hexvalue(v)) {
        return '?';
    }
    if (v >= 0 && v <= 9) {
        return '0' + v;
    }
    return 'a' + (v - 10);
}
/**
 * Return the value of a number's sign bit given the bit width. Follow the
 * detailed specification given in pa2.h.
 *
 * Hint: using < or > will not work!
 */
int get_sign_bit_value(struct bv128 bv, int bit_width) {
    int sign_bit_index = bit_width - 1;
    if (sign_bit_index < 64) {
        return (bv.lo >> sign_bit_index) & 1;
    }
    return (bv.hi >> (sign_bit_index - 64)) & 1;
}
/**
 * Convert an ASCII string containing hexadecimal digits (example: "0x5afb") to
 * a bv128 struct.
 *
 * see pa2.h for the detailed specifications of this function
 */
struct bv128 str_to_bv128(char str[], int str_len, int bit_width) {
    struct bv128 result = {0, 0, ERROR_NONE};

    /*
     * STEP 1: check for a badly formed hex string
     */
    if (bit_width < 1 || bit_width > 128) {
        result.error = ERROR_INVALID_ARGUMENTS;
        return result;
    }

    if (str_len < 3 || str[0] != '0' || (str[1] != 'x' && str[1] != 'X')) {
        result.error = ERROR_MALFORMED_ADDEND;
        return result;
    }

    /*
     * STEP 2: Check for potential overflow, then do hex string to binary conversion
     */
    int significant_digits = 0;
    int found_non_zero = 0;

    for (int i = 2; i < str_len; i++) {
        if (!is_hexdigit(str[i])) {
            result.error = ERROR_MALFORMED_ADDEND;
            return result;
        }

        if (found_non_zero || str[i] != '0') {
            found_non_zero = 1;
            significant_digits++;
        }
    }

    int bits_per_hex = 4;
    int max_hex_digits = (bit_width + bits_per_hex - 1) / bits_per_hex;

    if (significant_digits > max_hex_digits) {
        result.error = ERROR_ADDEND_OVERFLOW;
        return result;
    }

    int cur_bit_index = 0;
    uint64_t hi = 0, lo = 0;
    int pos = str_len - 1;

    while (pos >= 2 && cur_bit_index < max_hex_digits) {
        uint64_t digit_value = hexdigit_to_value(str[pos]);

        if (cur_bit_index < 16) {
            uint64_t old_lo = lo;
            lo |= digit_value << (cur_bit_index * bits_per_hex);
        } else {
            uint64_t old_hi = hi;
            hi |= digit_value << ((cur_bit_index - 16) * bits_per_hex);
        }
        pos--;
        cur_bit_index++;
    }

    /*
     * STEP 3: sign extension to a 128-bit two's complement number
     */
    int sign_bit_idx = bit_width - 1;
    uint64_t sign_bit_value;

    if (bit_width == 64) {
        sign_bit_value = (lo >> 63) & 1;
        result.lo = lo;
        result.hi = sign_bit_value ? ~0ULL : 0;
    } else if (bit_width < 64) {
        uint64_t mask = (1ULL << bit_width) - 1;
        sign_bit_value = (lo >> (bit_width - 1)) & 1;

        uint64_t high_bits = lo & ~mask;

        if (high_bits != 0 && (high_bits != ~mask || !sign_bit_value)) {
            result.error = ERROR_ADDEND_OVERFLOW;
            return result;
        }

        result.lo = sign_bit_value ? (lo | ~mask) : (lo & mask);
        result.hi = sign_bit_value ? ~0ULL : 0;
    } else {
        if (bit_width == 128) {
            result.lo = lo;
            result.hi = hi;
        } else {
            uint64_t hi_mask = (1ULL << (bit_width - 64)) - 1;
            sign_bit_value = (hi >> (bit_width - 65)) & 1;
            result.lo = lo;
            result.hi = sign_bit_value ? (hi | ~hi_mask) : (hi & hi_mask);
        }
    }

    return result;
}

/**
 * perform a 128-bit add on a + b
 * see pa2.h for the detailed specification of this function
*/

struct bv128 add_bv128(struct bv128 a, struct bv128 b) {
    struct bv128 result = {0, 0, ERROR_NONE};

    // Ignore error fields of a and b as stated in assumptions

    // Add the low 64 bits
    result.lo = a.lo + b.lo;

    // Check for carry from low to high
    int carry = (result.lo < a.lo) ? 1 : 0;

    // Add the high 64 bits with carry
    result.hi = a.hi + b.hi + carry;

    // Check for overflow at the 128-bit level
    int sign_a = get_sign_bit_value(a, 128);
    int sign_b = get_sign_bit_value(b, 128);
    int sign_result = get_sign_bit_value(result, 128);

    // In two's complement, overflow occurs when:
    // 1. Adding two positive numbers results in a negative number
    // 2. Adding two negative numbers results in a positive number
    if ((sign_a == sign_b) && (sign_result != sign_a)) {
        result.error = ERROR_SUM_OVERFLOW;
    }

    return result;
}

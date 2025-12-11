.text

/*****************************************************************************
 *                         Provided Example Functions
 *****************************************************************************/

/* uint8_t eq(int8_t a, int8_t b) |>
* Checks if two signed 8-bit integers are equal, returning either 1 if they are or 0 otherwise. */
.globl eq
eq:
        beq a0, a1, .equal
        mv a0, x0
        ret
.equal:
        addi a0, x0, 1
        ret

/* void ptr_eq(int8_t *a, int8_t *b, uint8_t *eq) |>
* Checks if the signed 8-bit numbers stored at two addresses are equal, storing a 1 at *eq if they are or 0 otherwise. */
.globl ptr_eq
ptr_eq:
        // save the return address
        addi sp, sp, -16 // allocate stack space; maintain 16B alignment
        sd ra, 8(sp)

        // call the comparison function
        lb a0, 0(a0)
        lb a1, 0(a1)
        call eq
        sb a0, 0(a2)

        // restore the return address
        ld ra, 8(sp)
        addi sp, sp, 16
        ret

/*****************************************************************************
 *                         Tier 1 Functions (Short)
 *****************************************************************************/

 /* uint8_t abs_val(int8_t a) |>
 * Get the absolute value of a signed 8-bit integer, returning a uint8_t. */
.globl abs_val
abs_val:
        // Sign-extend to 64 bits to handle signed operations properly
        slli a0, a0, 56
        srai a0, a0, 56

        // Check if a0 is negative
        bgez a0, .abs_positive

        // If negative, negate it (two's complement)
        neg a0, a0

.abs_positive:
        // Ensure the return value is properly masked to 8 bits
        andi a0, a0, 0xff
        ret

/* int8_t min(int8_t a, int8_t b) |>
 * Return the minimum of two signed 8-bit integers. */
.globl min
min:
        // Sign-extend both values for proper signed comparison
        slli a0, a0, 56
        srai a0, a0, 56
        slli a1, a1, 56
        srai a1, a1, 56

        // Compare a0 and a1
        blt a0, a1, .min_a0
        // If a1 is smaller or equal, return a1
        mv a0, a1

.min_a0:
       // a0 already contains the minimum
        ret

/* int8_t max(int8_t a, int8_t b) |>
* Return the maximum of two signed 8-bit integers. */
.globl max
max:
        // Sign-extend both values for proper signed comparison
        slli a0, a0, 56
        srai a0, a0, 56
        slli a1, a1, 56
        srai a1, a1, 56

       // Compare a0 and a1
       bgt a0, a1, .max_a0
       // If a1 is larger or equal, return a1
       mv a0, a1

.max_a0:
       // a0 already contains the maximum
       ret

/*****************************************************************************
 *                         Tier 2 Functions (Tall)
 *****************************************************************************/

/* uint16_t umul(uint8_t a, uint8_t b) |>
* Multiply two unsigned 8-bit integers into an unsigned 16-bit integer using multiply add. */
.globl umul
umul:
        // Zero-extend inputs to ensure proper unsigned handling
        andi a0, a0, 0xff
        andi a1, a1, 0xff

        // Initialize product to 0
        li a2, 0

        // If either input is 0, return 0
        beqz a0, .umul_done
        beqz a1, .umul_done
.umul_loop:
        // Check lowest bit of b
        andi a3, a1, 1
        beqz a3, .umul_skip_add

        // Add a to the product if lowest bit is 1
        add a2, a2, a0

.umul_skip_add:
        // Shift a left by 1 (double it)
        slli a0, a0, 1

        // Shift b right by 1
        srli a1, a1, 1

        // Continue until b becomes 0
        bnez a1, .umul_loop

.umul_done:
        // Move result to a0
        mv a0, a2
        ret

/* uint8_t udiv(uint16_t a, uint8_t b) |>
* Divide a by b (a is a 16-bit unsigned integer and b is a 8-bit unsigned integer) and
* return the result as an unsigned 8-bit integer using divide subtract.
* This is integer division, so if there is a remainder, it is discarded.
* If b is zero, return the equivalent of UINT8_MAX. */
.globl udiv
udiv:
        // Zero-extend inputs for proper unsigned handling 
	slli a0, a0, 48       // Shift left by 48 bits
	srli a0, a0, 48       // Shift right by 48 bits (keeps only lower 16 bits)
        andi a1, a1, 0xff     // 8-bit b

        // Check for division by zero
        beqz a1, .udiv_by_zero

        // Initialize quotient to 0
        li a2, 0

.udiv_loop:
        // if a < b, we're done
        bltu a0, a1, .udiv_done

        // Subtract b from a
        sub a0, a0, a1

        // Increment quotient
        addi a2, a2, 1

        // Continue loop
        j .udiv_loop

.udiv_by_zero:
        // Return UINT8_MAX (255) for division by zero
        li a2, 0xff

.udiv_done:
        // Move result to a0
        mv a0, a2
        ret

/* int8_t median(int8_t *a, uint8_t len) |>
* Obtain the median number in an array of signed 8-bit integers.
* Note that all arrays will have an odd size and be pre-sorted. */
.globl median
median:
        // If len is 0, return 0 (though problem says array will be odd length)
        beqz a1, .median_zero

        // Calculate middle index: len / 2
        srli a2, a1, 1

        //Access the middle element from the array
        add a0, a0, a2
        lb a0, 0(a0)
        ret

.median_zero:
	li a0, 0
	ret

/* int8_t vec_min(int8_t *a, uint8_t len) |>
* Obtain the minimum number in an array of signed 8-bit integers. If len = 0, return 0. */
.globl vec_min
vec_min:
        // If len is 0, return 0
	beqz a1, .vec_min_empty

	// Initialize min with the first element
        lb a2, 0(a0)

	// Initialize index 
	li a3, 1

.vec_min_loop: 
	// Check if we've processed all elements
	beq a3, a1, .vec_min_done

	// Load the next element
	add a4, a0, a3
	lb a4, 0(a4)

	// Compare with current min
	bge a4, a2, .vec_min_continue

	// Update min if current element is smaller
	mv a2, a4

.vec_min_continue:
	// Increment index
	addi a3, a3, 1

	// Continue loop
	j .vec_min_loop

.vec_min_empty:
	// Return 0 for empty array
	li a2, 0

.vec_min_done:
	// Move result to a0
	mv a0, a2
        ret

/* int8_t vec_max(int8_t *a, uint8_t len) |>
* Obtain the maximum number in an array of signed 8-bit integers. If len = 0, return 0. */
.globl vec_max
vec_max:
        // If len is 0, return 0
	beqz a1, .vec_max_empty

	// Initialize max with the first element
	lb a2, 0(a0)

	// Initialize index
	li a3, 1

.vec_max_loop:
	// Check if we've processed all elements
	beq a3, a1, .vec_max_done

	// Load the next element
	add a4, a0, a3
	lb a4, 0(a4)

	// Compare with current max
	ble a4, a2, .vec_max_continue

	// Update max if current element is larger 
	mv a2, a4

.vec_max_continue: 
	// Increment index
	addi a3, a3, 1

	// Continue loop
	j .vec_max_loop

.vec_max_empty:
	// Return 0 for empty array
	li a2, 0

.vec_max_done:
	// Move result to a0
	mv a0, a2
        ret

/* void range(int8_t *a, uint8_t len, int8_t *min, int8_t *max) |>
* Obtain the range of an array of signed 8-bit numbers (using vec_min and vec_max), and store it into min and max. If len = 0, store 0 in both. */
.globl range
range:
        // Save the return address and callee-saved registers
        addi sp, sp, -32
        sd ra, 24(sp)
        sd s0, 16(sp)
        sd s1, 8(sp)
        sd s2, 0(sp)

        // Save arguments
        mv s0, a0       // array pointer
        mv s1, a1       // length
        mv s2, a2       // min pointer
        // Save a3 as well for safety
        mv s3, a3       // max pointer

        // First check: if length is 0, go to empty case
        beqz s1, .range_empty

        // Second check: if array pointer is NULL, go to empty case
        beqz s0, .range_empty

        // Reach here if both array is non-NULL and length is non-zero
        j .range_non_empty
        
.range_empty:
        // Check min pointer for NULL before storing
        beqz s2, .skip_min_store_empty
        sb zero, 0(s2)  // Store 0 in min
.skip_min_store_empty:

        // Check max pointer for NULL before storing
        beqz s3, .skip_max_store_empty
        sb zero, 0(s3)  // Store 0 in max
.skip_max_store_empty:
        j .range_done

.range_non_empty:
        // Call vec_min with args: array and length
        mv a0, s0
        mv a1, s1
        call vec_min
        
        // Save result temporarily
        mv t0, a0
        
        // Check if min pointer is NULL
        beqz s2, .skip_min_store_result
        // Store vec_min result in min
        sb t0, 0(s2)
.skip_min_store_result:

        // Call vec_max with args: array and length
        mv a0, s0
        mv a1, s1
        call vec_max
        
        // Save result temporarily
        mv t0, a0
        
        // Check if max pointer is NULL
        beqz s3, .skip_max_store_result
        // Store vec_max result in max
        sb t0, 0(s3)
.skip_max_store_result:

.range_done:
        // Restore callee-saved registers and return
        ld ra, 24(sp)
        ld s0, 16(sp)
        ld s1, 8(sp)
        ld s2, 0(sp)
        addi sp, sp, 32
        ret


/*****************************************************************************
 *                        Tier 3 Functions (Grande)
 *****************************************************************************/

/* void vec_add(int8_t *a, int8_t *b, uint8_t len) |>
* Add the two vectors a and b (with lengths len) element-wise and put the result into a vector a. */
.globl vec_add
vec_add:
        // If len is 0, return
	beqz a2, .vec_add_done

	// Initialize index
	li a3, 0

.vec_add_loop:
	// Check if we've processed all elements
	beq a3, a2, .vec_add_done

	// Load elements from a and b
	add a4, a0, a3
	lb a5, 0(a4)	// a[i]

	add a6, a1, a3
	lb a6, 0(a6)	// b[i]

	// Add the elements
	add a5, a5, a6

	// Store result back in a
	sb a5, 0(a4)

	// Increment index
	addi a3, a3, 1

	// Continue loop
	j .vec_add_loop

.vec_add_done:
        ret

/* void vec_scale(uint8_t *a, uint8_t alpha, uint16_t *b, uint8_t len) |>
* Scale every element of a vector a by a factor alpha and put the result into a vector b. */
.globl vec_scale
vec_scale:
        // Save the return address and callee-saved registers
        addi sp, sp, -48
        sd ra, 40(sp)
        sd s0, 32(sp)
        sd s1, 24(sp)
        sd s2, 16(sp)
        sd s3, 8(sp)
        sd s4, 0(sp)

        // Save arguments in saved registers to preserve across function calls
        mv s0, a0       // source array pointer
        mv s1, a1       // scale factor
        mv s2, a2       // destination array pointer
        mv s3, a3       // length
        
        // If len is 0 or pointers are NULL, return
        beqz s3, .vec_scale_done
        beqz s0, .vec_scale_done
        beqz s2, .vec_scale_done
        
        // Initialize index
        li s4, 0

.vec_scale_loop:
        // Check if we've processed all elements
        beq s4, s3, .vec_scale_done
        
        // Load element from source array
        add t0, s0, s4
        lbu t1, 0(t0)   // Load byte from source[i]
        
        // Set up arguments for umul
        mv a0, t1       // Source value
        mv a1, s1       // Scale factor
        
        // Call umul
        call umul
        
        // Result is in a0, calculate destination address
        slli t0, s4, 1  // Multiply index by 2 for 16-bit elements
        add t0, s2, t0  // Add to destination base
        sh a0, 0(t0)    // Store halfword result
        
        // Increment index and continue
        addi s4, s4, 1
        j .vec_scale_loop

.vec_scale_done:
        // Restore callee-saved registers and return
        ld ra, 40(sp)
        ld s0, 32(sp)
        ld s1, 24(sp)
        ld s2, 16(sp)
        ld s3, 8(sp)
        ld s4, 0(sp)
        addi sp, sp, 48
        ret

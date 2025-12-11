#include "pa5_cache.h"

/*****************************************************************************
 *                      PROVIDED UTILITY FUNCTIONS                           *
 *****************************************************************************/
/*
 * checks whether x is a power of 2 (exculding zero)
 */
bool is_po2(uint64_t x)
{
    return (x != 0) && ((x & (x - 1)) == 0);
}

/*
 * computes how many bits are required to represent the given unsigned number 
 */
int64_t nbits_required(uint64_t x)
{
    int64_t r = 0;
    while(x >>= 1)
        r++;
    return r;
}

/*****************************************************************************
 *                   RECOMMENDED UTILITY FUNCTIONS                           *
 *                                                                           *
 * We recommend you implement these to make the fetch() and memory() stage   *
 * cleaner. These will not be graded, but we provide some example assertion- *
 * based tests for them in `main.c`                                          *
 *****************************************************************************/
/*
 * calculates which byte position in a cache block the address points to (i.e.,
 * extracts the byte offset into the cache block)
 */
uint64_t get_byte_offset_within_cache_block(uint64_t address)
{
    // Extract the lower bits that represent the byte offset within a block
    return address & (CACHE_BLOCK_SIZE_BYTES - 1);
}

/*
 * calculates which cache set the address belongs to (i.e., extracts the set
 * index field of the address)
 */
uint64_t get_cache_set_index(uint64_t address)
{
// Extract the set index bits
    return (address >> NUM_BITS_BYTE_OFFSET_IN_BLOCK) & (CACHE_NUM_SETS - 1);
}

/*
 * returns the cache block tag (i.e., extracts the tag field of the address)
 */
uint64_t get_cache_block_tag(uint64_t address)
{
    // Extract the tag bits (everything above offset and set index)
    return address >> (NUM_BITS_BYTE_OFFSET_IN_BLOCK + NUM_BITS_SET_INDEX);
}

/*****************************************************************************
 *                   REQUIRED + GRADED FUNCTIONS                             *
 *****************************************************************************/
/*
 * copies a cache block's worth of data from main memory into the cache
 * 
 * inputs:
 *     block: the cache block to fill
 *     address: the memory address that the original ld/sd instruction attempted to access
 *     mem_read_byte: a function for reading one byte from memory at a specific address
 * assumptions:
 *     address will be a multiple of 8 bytes
 */
void cache_block_fill(struct cache_block *block, uint64_t address, uint8_t (*mem_read_byte)(uint64_t addr))
{
// Calculate base address of the block in memory (zeroing out the offset bits)
    uint64_t block_base_addr = address & ~(CACHE_BLOCK_SIZE_BYTES - 1);
    
    // Update block tag
    block->tag = get_cache_block_tag(address);
    
    // Read data from memory byte by byte
    for (int i = 0; i < CACHE_BLOCK_SIZE_BYTES; i++) {
        block->data[i] = mem_read_byte(block_base_addr + i);
    }
    
    // Update block state to CLEAN
    block->state = STATE_CLEAN;
}

/*
 * copies a cache block's worth of data from the cache into main memory
 * 
 * inputs:
 *     block: the cache block to write back to memory
 *     address: the memory address that the original ld/sd instruction attempted to access
 *     mem_write_byte: a function for writing one byte to memory at a specific address
 * assumptions:
 *     address will be a multiple of 8 bytes
 */
void cache_block_writeback(struct cache_block *block, uint64_t address, void (*mem_write_byte)(uint64_t addr, uint8_t data))
{
// Calculate base address for this block in memory
    uint64_t set_index = get_cache_set_index(address);
    uint64_t block_base_addr = (block->tag << (NUM_BITS_BYTE_OFFSET_IN_BLOCK + NUM_BITS_SET_INDEX)) | 
                              (set_index << NUM_BITS_BYTE_OFFSET_IN_BLOCK);
    
    // Write data to memory byte by byte
    for (int i = 0; i < CACHE_BLOCK_SIZE_BYTES; i++) {
        mem_write_byte(block_base_addr + i, block->data[i]);
    }
    
    // Update block state to CLEAN
    block->state = STATE_CLEAN;
}

/*
 * reads a dword (64 bits) from the cache, performing any necessary writeback or
 * fill operations to ensure that dirty data is not lost and that the correct
 * data is accessed.
 *
 * inputs:
 *     address: the address of the dword being read
 *     mem_read_byte: a function pointer to a function that allows reading a single byte from memory
 *     mem_write_byte: a function pointer to a function that allows writing a single byte to memory
 * outputs:
 *     out: the desired dword (either coming from cache or memory)
 * assumptions:
 *     address will be a multiple of 8 bytes
 */
uint64_t cache_read(uint64_t address, uint8_t (*mem_read_byte)(uint64_t addr), void (*mem_write_byte)(uint64_t addr, uint8_t data))
{
// Get cache block for this address
    uint64_t set_index = get_cache_set_index(address);
    struct cache_block *block = &g_cache[set_index];
    uint64_t tag = get_cache_block_tag(address);
    
    // Perform cache operations based on current state
    switch(block->state)
    {
        case STATE_INVALID:
            // Cache miss - fill the block from memory
            cache_block_fill(block, address, mem_read_byte);
            break;
        
        case STATE_CLEAN:
            // Check if it's a hit
            if (block->tag != tag) {
                // Cache miss (wrong tag) - fill the block from memory
                cache_block_fill(block, address, mem_read_byte);
            }
            break;
        
        case STATE_DIRTY:
            // Check if it's a hit
            if (block->tag != tag) {
                // Cache miss (wrong tag) - writeback and then fill
                cache_block_writeback(block, address, mem_write_byte);
                cache_block_fill(block, address, mem_read_byte);
            }
            break;
        
        default:
            printf("error: unrecognized cache block state: %d\n", block->state);
            return 0;
    }
    
    // Get byte offset within the block
    uint64_t offset = get_byte_offset_within_cache_block(address);
    
    // Read the dword from cache block (ensure we're aligned to dword boundaries)
    uint64_t result = 0;
    for (int i = 0; i < 8; i++) {
        result |= (uint64_t)block->data[offset + i] << (i * 8);
    }
    
    return result;
}

/*
 * writes a dword (64 bits) to the cache, performing any necessary writeback or
 * fill operations to ensure that dirty data is not lost and that the correct
 * data is accessed.
 *
 * inputs:
 *     address: the address of the dword being written
 *     data: the dword to be written
 *     mem_read_byte: a function pointer to a function that allows reading a single byte from memory
 *     mem_write_byte: a function pointer to a function that allows writing a single byte to memory
 * assumptions: 
 *     address will be a multiple of 8 bytes
 */
void cache_write(uint64_t address, uint64_t data, uint8_t (*mem_read_byte)(uint64_t addr), void (*mem_write_byte)(uint64_t addr, uint8_t data))
{
// Get cache block for this address
    uint64_t set_index = get_cache_set_index(address);
    struct cache_block *block = &g_cache[set_index];
    uint64_t tag = get_cache_block_tag(address);
    
    // Perform cache operations based on current state
    switch(block->state)
    {
        case STATE_INVALID:
            // Cache miss - fill the block from memory first
            cache_block_fill(block, address, mem_read_byte);
            break; // Explicit break to avoid fallthrough
        
        case STATE_CLEAN:
        case STATE_DIRTY:
            // Check if it's a hit
            if (block->tag != tag) {
                // Cache miss (wrong tag)
                if (block->state == STATE_DIRTY) {
                    // Writeback the current block first
                    cache_block_writeback(block, address, mem_write_byte);
                }
                // Then fill the new block
                cache_block_fill(block, address, mem_read_byte);
            }
            break;
            
        default:
            printf("error: unrecognized cache block state: %d\n", block->state);
            return;
    }
    
    // Get byte offset within the block
    uint64_t offset = get_byte_offset_within_cache_block(address);
    
    // Write the dword to the cache block (byte by byte)
    for (int i = 0; i < 8; i++) {
        block->data[offset + i] = (data >> (i * 8)) & 0xFF;
    }
    
    // Mark the block as dirty
    block->state = STATE_DIRTY;
}

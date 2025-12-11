#include "pa5_cpu.h"
#include <string.h>

/*****************************************************************************
 *                      PROVIDED UTILITY FUNCTIONS                           *
 *****************************************************************************/
// extract the immediate from an instruction
uint64_t extract_immediate(uint32_t inst)
{
    uint32_t opcode = extract_bits(inst, 6, 0);
    uint64_t imm = 0;

    switch(opcode)
    {
        case RV_OPCODE_OP: return 0;

        case RV_OPCODE_OP_IMM:
        case RV_OPCODE_LOAD:
            imm = extract_bits(inst, 31, 20);
            return imm >> 11 ? (-1ul << 11) | imm : imm;

        case RV_OPCODE_STORE:
            imm = (extract_bits(inst, 31, 25) << 5) | extract_bits(inst, 11, 7);
            return imm >> 11 ? (-1ul << 11) | imm : imm;

        case RV_OPCODE_BRANCH:
            imm = (extract_bits(inst, 31, 31) << 12) | (extract_bits(inst, 7, 7) << 11) | (extract_bits(inst, 30, 25) << 5) | (extract_bits(inst, 11, 8) << 1);
            return imm >> 12 ? (-1ul << 12) | imm : imm;

        default:
            printf("error: cannot extract immediate from instruction of unknown itype\n");
            return 0;
    }
}

// extract some range of bits from an instruction (indices are right to left)
uint16_t extract_bits(uint32_t inst, uint8_t msb_idx, uint8_t lsb_idx)
{
    return (inst >> lsb_idx) & ((1u << (msb_idx - lsb_idx + 1)) - 1);
}

/*****************************************************************************
 *                   RECOMMENDED UTILITY FUNCTIONS                           *
 *                                                                           *
 * We recommend you implement these to make the fetch() and memory() stage   *
 * cleaner. These will not be graded, but feel free to define assert-based   *
 * tests for them if you like.                                               *
 *****************************************************************************/
/*
 * use the function pointer rd_fn to read the RISC-V instruction from memory at
 * the given program counter 
 */
uint32_t read_instruction(uint64_t pc, uint8_t (*rd_fn)(uint64_t addr))
{
    uint32_t inst = 0;
    for (int i = 0; i < 4; i++) {
        inst |= (uint32_t)rd_fn(pc + i) << (i * 8); // Little-endian
    }
    return inst;
}


// use the function pointer rd_fn to read the dword at the given memory address
uint64_t read_dword(uint64_t addr, uint8_t (*rd_fn)(uint64_t addr))
{
    uint64_t data = 0;
    for (int i = 0; i < 8; i++) {
        data |= (uint64_t)rd_fn(addr + i) << (i * 8); // Little-endian
    }
    return data;
}

// use the function pointer wr_fn to write the dword "data" to the given memory address
void write_dword(uint64_t addr, uint64_t data, void (*wr_fn)(uint64_t addr, uint8_t data))
{
    for (int i = 0; i < 8; i++) {
        wr_fn(addr + i, (data >> (i * 8)) & 0xFF); // Little-endian
    }
}

/*****************************************************************************
 *                                                                           *
 *                   REQUIRED + GRADED FUNCTIONS                             *
 *                                                                           *
 *****************************************************************************/

/*****************************************************************************
 *                                   FETCH                                   *
 *****************************************************************************/
/*
 * simulates the fetch stage
 *
 * inputs:
 *     pc: the program counter for this cycle
 *     mem_read_byte: a function pointer to a function that allows reading a single byte from memory
 * outputs:
 *     out: the outputs of the fetch stage (defined in types.h)
 * assumptions:
 *     - the pc will be a multiple of 4 bytes
 */
void pa5_fetch(uint64_t pc, uint8_t (*mem_read_byte)(uint64_t addr), struct fetch_outputs *out)
{
    out->inst = read_instruction(pc, mem_read_byte);
    out->pc = pc;
}

/*****************************************************************************
 *                                  DECODE                                   *
 *****************************************************************************/
/*
 * decode the ALU operation from the instruction
 *
 * inputs:
 *     inst: the instruction to decode
 * outputs:
enum ALU_OP get_aluop(uint32_t inst)
{
    uint32_t opcode = extract_bits(inst, 6, 0);
    uint32_t funct3 = extract_bits(inst, 14, 12);
    uint32_t funct7 = extract_bits(inst, 31, 25);
    
    switch (opcode) {
        case RV_OPCODE_OP: // R-type
            switch (funct3) {
                case FUNCT3_ADD_SUB:
                    if (funct7 == FUNCT7_SUB) {
                        return ALU_OP_SUB;
                    } else {
                        return ALU_OP_ADD;
                    }
                case FUNCT3_XOR:
                    return ALU_OP_XOR;
                case FUNCT3_OR:
                    return ALU_OP_OR;
                case FUNCT3_AND:
                    return ALU_OP_AND;
                default:
                    return ALU_OP_ADD;
            }
            
        case RV_OPCODE_OP_IMM: // I-type
            switch (funct3) {
                case FUNCT3_ADD_SUB:
                    return ALU_OP_ADD;
                case FUNCT3_XOR:
                    return ALU_OP_XOR;
                case FUNCT3_OR:
                    return ALU_OP_OR;
                case FUNCT3_AND:
                    return ALU_OP_AND;
                default:
                    return ALU_OP_ADD;
            }
            
        default:
            return ALU_OP_ADD;
    }
}
 *     out: the ALU operation (enums are defined in types.h)
 */
enum ALU_OP get_aluop(uint32_t inst)
{
    uint32_t opcode = extract_bits(inst, 6, 0);
    uint32_t funct3 = extract_bits(inst, 14, 12);
    uint32_t funct7 = extract_bits(inst, 31, 25);
    
    switch (opcode) {
        case RV_OPCODE_OP: // R-type
            switch (funct3) {
                case FUNCT3_ADD_SUB:
                    if (funct7 == FUNCT7_SUB) {
                        return ALU_OP_SUB;
                    } else {
                        return ALU_OP_ADD;
                    }
                case FUNCT3_XOR:
                    return ALU_OP_XOR;
                case FUNCT3_OR:
                    return ALU_OP_OR;
                case FUNCT3_AND:
                    return ALU_OP_AND;
                default:
                    return ALU_OP_ADD;
            }
            
        case RV_OPCODE_OP_IMM: // I-type
            switch (funct3) {
                case FUNCT3_ADD_SUB:
                    return ALU_OP_ADD;
                case FUNCT3_XOR:
                    return ALU_OP_XOR;
                case FUNCT3_OR:
                    return ALU_OP_OR;
                case FUNCT3_AND:
                    return ALU_OP_AND;
                default:
                    return ALU_OP_ADD;
            }
            
        default:
            return ALU_OP_ADD;
    }
}

/*
 * decode the branch condition from the instruction
 *
 * inputs:
 *     inst: the instruction to decode
 * outputs:
 *     out: the branch condition (enums are defined in types.h)
 */
enum BR_COND get_br_cond(uint32_t inst)
{
    uint32_t opcode = extract_bits(inst, 6, 0);
    uint32_t funct3 = extract_bits(inst, 14, 12);
    
    if (opcode == RV_OPCODE_BRANCH) {
        switch (funct3) {
            case FUNCT3_BEQ:  return BR_COND_EQ;
            case FUNCT3_BNE:  return BR_COND_NEQ;
            case FUNCT3_BLT:  return BR_COND_LT;
            case FUNCT3_BGE:  return BR_COND_GE;
            case FUNCT3_BLTU: return BR_COND_LTU;
            case FUNCT3_BGEU: return BR_COND_GEU;
            default:          return BR_COND_NEVER;
        }
    }
    
    return BR_COND_NEVER;
}
/*
 * simulates the decode stage
 *
 * inputs:
 *     in: the outputs of the fetch stage (that are inputs to the decode stage)
 *     gprs: x0 - x31 (the general-purpose registers) READ-ONLY
 * outputs:
 *     out: the outputs of the decode stage (defined in types.h)
 * assumptions:
 *     - there will always be NUM_GPRS elements in the gprs array
 */
void pa5_decode(const struct fetch_outputs *in, const uint64_t *gprs, struct decode_outputs *out)
{
    // Zero out the struct completely
    memset(out, 0, sizeof(struct decode_outputs));
    
    uint32_t inst = in->inst;
    uint32_t opcode = extract_bits(inst, 6, 0);
    uint32_t rd = extract_bits(inst, 11, 7);
    uint32_t rs1 = extract_bits(inst, 19, 15);
    uint32_t rs2 = extract_bits(inst, 24, 20);
    
    // Set basic values that are always needed
    out->pc = in->pc;
    out->rd_idx = (enum GPR_IDX)rd;
    out->rs1 = (rs1 == 0) ? 0 : gprs[rs1];
    out->rs2 = (rs2 == 0) ? 0 : gprs[rs2];
    out->imm = extract_immediate(inst);
    
    // Set default control signals (for all instruction types)
    out->a_sel = A_SEL_RS1;
    out->b_sel = B_SEL_RS2;
    out->alu_op = ALU_OP_ADD;
    out->br_cond = BR_COND_NEVER;
    out->wb_sel = WB_SEL_ALU_RESULT;
    out->reg_write_en = false;
    out->mem_read_en = false;
    out->mem_write_en = false;
    
    // Set specific control signals based on opcode
    switch (opcode) {
        case RV_OPCODE_OP:  // R-type
            out->a_sel = A_SEL_RS1;
            out->b_sel = B_SEL_RS2;
            out->alu_op = get_aluop(inst);
            out->wb_sel = WB_SEL_ALU_RESULT;
            out->reg_write_en = true;
            out->mem_read_en = false;
            out->mem_write_en = false;
            out->br_cond = BR_COND_NEVER;
            break;
            
        case RV_OPCODE_OP_IMM:  // I-type arithmetic
            out->a_sel = A_SEL_RS1;
            out->b_sel = B_SEL_IMM;
            out->alu_op = get_aluop(inst);
            out->wb_sel = WB_SEL_ALU_RESULT;
            out->reg_write_en = true;
            out->mem_read_en = false;
            out->mem_write_en = false;
            out->br_cond = BR_COND_NEVER;
            break;
            
        case RV_OPCODE_LOAD:  // Load
            out->a_sel = A_SEL_RS1;
            out->b_sel = B_SEL_IMM;
            out->alu_op = ALU_OP_ADD;
            out->wb_sel = WB_SEL_MEM_READ_DATA;
            out->reg_write_en = true;
            out->mem_read_en = true;
            out->mem_write_en = false;
            out->br_cond = BR_COND_NEVER;
            break;
            
        case RV_OPCODE_STORE:  // Store
            out->a_sel = A_SEL_RS1;
            out->b_sel = B_SEL_IMM;
            out->alu_op = ALU_OP_ADD;
            out->wb_sel = WB_SEL_ALU_RESULT; // Default but not used
            out->reg_write_en = false;
            out->mem_read_en = false;
            out->mem_write_en = true;
            out->br_cond = BR_COND_NEVER;
            break;
            
        case RV_OPCODE_BRANCH:  // Branch
            out->a_sel = A_SEL_PC;
            out->b_sel = B_SEL_IMM;
            out->alu_op = ALU_OP_ADD;
            out->wb_sel = WB_SEL_ALU_RESULT; // Default but not used
            out->reg_write_en = false;
            out->mem_read_en = false;
            out->mem_write_en = false;
            out->br_cond = get_br_cond(inst);
            break;
    }
    
    // 1. Ensure x0 is never written to
    if (rd == 0) {
        out->reg_write_en = false;
    }
}
/*****************************************************************************
 *                        ADDRESS GENERATION + EXECUTE                       *
 *****************************************************************************/
/*
 * simulates the agex stage
 *
 * inputs:
 *     in: the outputs of the decode stage
 * outputs:
 *     out: the outputs of the agex stage (defined in types.h)
 */
void pa5_agex(const struct decode_outputs *in, struct agex_outputs *out)
{
// Forward control signals
    out->rd_idx = in->rd_idx;
    out->wb_sel = in->wb_sel;
    out->reg_write_en = in->reg_write_en;
    out->mem_read_en = in->mem_read_en;
    out->mem_write_en = in->mem_write_en;
    out->pc = in->pc;
    out->rs2 = in->rs2;
    
    // Select ALU inputs
    uint64_t alu_a = (in->a_sel == A_SEL_PC) ? in->pc : in->rs1;
    uint64_t alu_b = (in->b_sel == B_SEL_IMM) ? in->imm : in->rs2;
    
    // Perform ALU operation
    switch (in->alu_op) {
        case ALU_OP_ADD:
            out->alu_result = alu_a + alu_b;
            break;
        case ALU_OP_SUB:
            out->alu_result = alu_a - alu_b;
            break;
        case ALU_OP_XOR:
            out->alu_result = alu_a ^ alu_b;
            break;
        case ALU_OP_OR:
            out->alu_result = alu_a | alu_b;
            break;
        case ALU_OP_AND:
            out->alu_result = alu_a & alu_b;
            break;
        default:
            out->alu_result = alu_a + alu_b;
            break;
    }
    
    // Evaluate branch condition
    bool take_branch = false;
    
    switch (in->br_cond) {
        case BR_COND_EQ:
            take_branch = (in->rs1 == in->rs2);
            break;
        case BR_COND_NEQ:
            take_branch = (in->rs1 != in->rs2);
            break;
        case BR_COND_LT:
            take_branch = ((int64_t)in->rs1 < (int64_t)in->rs2);
            break;
        case BR_COND_GE:
            take_branch = ((int64_t)in->rs1 >= (int64_t)in->rs2);
            break;
        case BR_COND_LTU:
            take_branch = (in->rs1 < in->rs2);
            break;
        case BR_COND_GEU:
            take_branch = (in->rs1 >= in->rs2);
            break;
        case BR_COND_ALWAYS:
            take_branch = true;
            break;
        case BR_COND_NEVER:
            take_branch = false;
            break;
        case BR_COND_NONE:
            take_branch = false;
            break;
    }    
    // Set PC selection based on branch condition
    out->pc_sel = take_branch ? PC_SEL_ALU_RESULT : PC_SEL_PC_PLUS_4;
}
/*****************************************************************************
 *                                 MEMORY                                    *
 *****************************************************************************/
/*
 * simulates the memory stage
 *
 * inputs:
 *     in: the outputs of the agex stage
 *     mem_read_byte: a function pointer to a function that allows reading a single byte from memory
 *     mem_write_byte: a function pointer to a function that allows writing a single byte to memory
 * outputs:
 *     out: the outputs of the memory stage (defined in types.h)
 * assumptions:
 *     - the memory address to access will always be a multiple of 8 bytes
 */
void pa5_mem(const struct agex_outputs *in,
    uint8_t (*mem_read_byte)(uint64_t addr),
    void (*mem_write_byte)(uint64_t addr, uint8_t data),
    struct mem_outputs *out,
    bool use_cache)
{

(void) use_cache; //mark parameter as intentionally unused
// Forward control signals
    out->rd_idx = in->rd_idx;
    out->wb_sel = in->wb_sel;
    out->reg_write_en = in->reg_write_en;
    out->pc_sel = in->pc_sel;
    out->pc = in->pc;
    out->alu_result = in->alu_result;
    
    // Memory operations
    uint64_t mem_addr = in->alu_result;
    
    if (in->mem_read_en) {
        out->mem_rdata = read_dword(mem_addr, mem_read_byte);
    } else {
        out->mem_rdata = 0;
    }
    
    if (in->mem_write_en) {
        write_dword(mem_addr, in->rs2, mem_write_byte);
    }
}

/*****************************************************************************
 *                               WRITEBACK                                   *
 *****************************************************************************/
/*
 * simulates the writeback stage
 *
 * inputs:
 *     in: the outputs of the memory stage
 *     gprs: x0 - x31 (the general-purpose registers)
 * outputs:
 *     out: the outputs of the writeback stage (defined in types.h)
 * assumptions: 
 *     - there will always be NUM_GPRS elements in the gprs array
 */
void pa5_writeback(const struct mem_outputs *in, uint64_t *gprs, struct writeback_outputs *out)
{
    // Determine write data
    uint64_t write_data = 0;
    
    if (in->wb_sel == WB_SEL_ALU_RESULT) {
        write_data = in->alu_result;
    } else if (in->wb_sel == WB_SEL_MEM_READ_DATA) {
        write_data = in->mem_rdata;
    }
    
    // Update register file
    if (in->reg_write_en && in->rd_idx != X0_ZERO) { // x0 is always 0
        gprs[in->rd_idx] = write_data;
    }
    
    // Determine next PC
    if (in->pc_sel == PC_SEL_PC_PLUS_4) {
        out->next_pc = in->pc + 4;
    } else if (in->pc_sel == PC_SEL_ALU_RESULT) {
        out->next_pc = in->alu_result;
    } else {
        out->next_pc = in->pc + 4; // Default
    }
}

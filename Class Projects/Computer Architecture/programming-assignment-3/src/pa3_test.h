#ifndef PA3_TEST_H
#define PA3_TEST_H

#include "pa3.h"

enum test_result {
    TEST_RESULT_PASS,
    TEST_RESULT_FAIL,
    TEST_RESULT_FAIL_TO_DESERIALIZE_FILESYSTEM,
    TEST_RESULT_UNABLE_TO_OPEN_TEST_FILE,
	TEST_RESULT_UNIMPLEMENTED
};

void report_test_result(enum test_result tr);

/* Test functions. Some are currently unimplemented */
enum test_result fs0_test(struct fs_node *root);
enum test_result fs1_test(struct fs_node *root);
enum test_result fs2_test(struct fs_node *root);
enum test_result fs3_test(struct fs_node *root);

void *read_serialized_filesystem_file(const char *serialized_filename);

struct filesystem_test_config
{
    const char *serialized_filename;
    enum test_result (*test_func)(struct fs_node *); 
};

#endif /* PA3_TEST_H */
#include "pa3.h"

/* ======================================================================
 * Provided example functions
 * ====================================================================== */
// Print a given node's data
void pa3_print_node_data(const struct fs_node * const node) {
    printf("%s {props=[type:%c, perms:%c%c], num_children=%d} @ [%p]\n",
        node->name,
        (node->props & P_FILE_BIT) ? 'F' : 'D',
        (node->props & P_READ_BIT) ? 'R' : '-',
        (node->props & P_WRITE_BIT) ? 'W' : '-',
        node->num_children,
        (void *) node);
}

// Implementation of print_tree
void pa3_print_tree_impl(const struct fs_node * const node, int depth) {
    printf("%*s> ", depth * 4, "");
    pa3_print_node_data(node);
    for (int child_idx = 0; child_idx < node->num_children; child_idx++)
        pa3_print_tree_impl(node->child_list[child_idx], depth + 1);
}

// Print a tree's data in a digestible format. You are encouraged to make use of this in your testing!
void pa3_print_tree(const struct fs_node * const root) {
    if (root == NULL) {
        printf("[ERROR] trying to print a NULL tree\n");
        return;
    }
    pa3_print_tree_impl(root, 0);
}


// Given a pointer to the start of a node's data, return its props field as a uint8_t
uint8_t get_props(void *node_data) {
    return *((uint8_t *) node_data);
}

// Given a pointer to the start of a node's data, return its num_children field as a uint8_t
uint8_t get_num_children(void *node_data) {
    return *((uint8_t *) node_data + sizeof(uint8_t));
}

/* ======================================================================
 * Your implementations start here! For detailed explanations of each
 * function, either look in pa3.h or consult the Google Doc.
 * Good luck! :)
 *
 * IMPORTANT NOTE: You may wish to define some static or global data
 * structures to hold your data to make some aspect of this assignment
 * easier. If you do so, the autograder will be unable to properly
 * run your code due to how it is implemented, and you may receive a 0,
 * so please avoid this.
 * ====================================================================== */

#define NODE_SIZE_BYTES (2 + MAX_NAME_LENGTH + (MAX_NUM_CHILDREN_PER_NODE * sizeof(uint16_t)))

enum error_code pa3_extract_node_name(char **name, char *name_data) {
    if (name_data == NULL) {
        return ERROR_INVALID_OPERATION;
    }

    // Find the length of the name (up to MAX_NAME_LENGTH)
    size_t name_len = 0;
    while (name_len < MAX_NAME_LENGTH && name_data[name_len] != '\0') {
        name_len++;
    }

    // Allocate space for the name (+1 for null terminator)
    *name = (char *)malloc(name_len + 1);
    if (*name == NULL) {
        return ERROR_MALLOC_FAIL;
    }

    // Copy the name and ensure it's null-terminated
    memcpy(*name, name_data, name_len);
    (*name)[name_len] = '\0';

    return ERROR_NONE;
}
// More accurate node data calculation
void *pa3_node_data_by_index(uint16_t idx, void *data) {
    if (data == NULL) {
        return NULL;
    }

    // Calculate correct offset based on node structure
    size_t node_size = 2 + MAX_NAME_LENGTH + (MAX_NUM_CHILDREN_PER_NODE * sizeof(uint16_t));
    return (uint8_t *)data + (idx * node_size);
}

enum error_code pa3_deserialize_node(struct fs_node **node, uint16_t idx, void *data) {
    if (data == NULL) {
        return ERROR_INVALID_OPERATION;
    }

    // Calculate offset directly using byte offsets instead of struct casting
    size_t node_size = 2 + MAX_NAME_LENGTH + (MAX_NUM_CHILDREN_PER_NODE * sizeof(uint16_t));
    uint8_t *current_node = (uint8_t *)data + (idx * node_size);
    
    // Allocate memory for the node
    *node = (struct fs_node *)malloc(sizeof(struct fs_node));
    if (*node == NULL) {
        return ERROR_MALLOC_FAIL;
    }

    // Extract properties and number of children directly from bytes
    (*node)->props = current_node[0];  // First byte is props
    (*node)->num_children = current_node[1];  // Second byte is num_children
    
    // Ensure num_children is within bounds
    if ((*node)->num_children > MAX_NUM_CHILDREN_PER_NODE) {
        (*node)->num_children = MAX_NUM_CHILDREN_PER_NODE;
    }
    
    // Extract name - starts at offset 2
    char *name_data = (char *)(current_node + 2);
    enum error_code name_result = pa3_extract_node_name(&((*node)->name), name_data);
    if (name_result != ERROR_NONE) {
        free(*node);
        *node = NULL;
        return name_result;
    }
    
    // Process children if any
    if ((*node)->num_children > 0) {
        // Allocate child list
        (*node)->child_list = (struct fs_node **)calloc((*node)->num_children, sizeof(struct fs_node *));
        if ((*node)->child_list == NULL) {
            free((*node)->name);
            free(*node);
            *node = NULL;
            return ERROR_MALLOC_FAIL;
        }
        
        // Child indices start after the name field
        uint16_t *child_indices = (uint16_t *)(name_data + MAX_NAME_LENGTH);
        
        // Process each child
        for (int i = 0; i < (*node)->num_children; i++) {
            // Skip self-references or invalid indices
            if (child_indices[i] == idx) {
                // Clean up already processed children
                for (int j = 0; j < i; j++) {
                    pa3_free_tree((*node)->child_list[j]);
                }
                free((*node)->child_list);
                free((*node)->name);
                free(*node);
                *node = NULL;
                return ERROR_INVALID_OPERATION;
            }
            
            // Recursively deserialize child
            enum error_code child_result = pa3_deserialize_node(
                &((*node)->child_list[i]),
                child_indices[i],
                data
            );
            
            if (child_result != ERROR_NONE) {
                // Clean up on error
                for (int j = 0; j < i; j++) {
                    pa3_free_tree((*node)->child_list[j]);
                }
                free((*node)->child_list);
                free((*node)->name);
                free(*node);
                *node = NULL;
                return child_result;
            }
        }
    } else {
        (*node)->child_list = NULL;
    }
    
    return ERROR_NONE;
}
void pa3_free_tree(struct fs_node *root) {
    if (root == NULL) return;

    // First free all children recursively
    if (root->child_list != NULL) {
        for (int i = 0; i < root->num_children; i++) {
            if (root->child_list[i] != NULL) {  // Added NULL check
                pa3_free_tree(root->child_list[i]);
            }
        }
        free(root->child_list);
    }

    // Free the node's name and the node itself
    if (root->name != NULL) {  // Added NULL check
        free(root->name);
    }
    free(root);
}

enum error_code pa3_navigate_to_node(struct fs_node *root, struct fs_node **found, const char *path) {
    if (root == NULL || path == NULL || found == NULL) {
        return ERROR_NOT_FOUND;
    }

    // Handle root directory case
    if (strcmp(path, "/") == 0) {
        *found = root;
        return ERROR_NONE;
    }

    // Special case: Check if path is just "/root" and root's name is "root"
    if (strcmp(path, "/root") == 0 && strcmp(root->name, "root") == 0) {
        *found = root;
        return ERROR_NONE;
    }

    // Skip leading "/" if present
    const char *curr_path = path;
    if (path[0] == PATH_SEPARATOR) {
        curr_path = path + 1;
    }

    char *sep = strchr(curr_path, PATH_SEPARATOR);
    if (sep) {
        // Extract the current directory name
        int dir_name_len = sep - curr_path;
        char *dir_name = (char *)malloc(dir_name_len + 1);
        if (dir_name == NULL) {
            return ERROR_MALLOC_FAIL;
        }

        strncpy(dir_name, curr_path, dir_name_len);
        dir_name[dir_name_len] = '\0';

        // Special case: Check if we're at root level and the dir_name matches root's name
        if (strcmp(dir_name, root->name) == 0) {
            free(dir_name);
            return pa3_navigate_to_node(root, found, sep);
        }

        // Find the child with matching name
        struct fs_node *next_node = NULL;
        for (int i = 0; i < root->num_children; i++) {
            if (strcmp(root->child_list[i]->name, dir_name) == 0) {
                next_node = root->child_list[i];
                break;
            }
        }

        free(dir_name);
        if (next_node == NULL) {
            return ERROR_NOT_FOUND;
        }

        // Can't navigate into a file
        if (next_node->props & P_FILE_BIT) {
            return ERROR_INVALID_OPERATION;
        }

        if (!(next_node->props & P_READ_BIT)) {
            // No read permission
            return ERROR_MISSING_PERMISSIONS;
        }

        // Recursively navigate to the next part of the path
        return pa3_navigate_to_node(next_node, found, sep);
    } else {
        // Special case: If the current path component matches the root name
        if (strcmp(curr_path, root->name) == 0) {
            *found = root;
            return ERROR_NONE;
        }

        // We're at the last component of the path
        // Find the child with matching name
        for (int i = 0; i < root->num_children; i++) {
            if (strcmp(root->child_list[i]->name, curr_path) == 0) {
                *found = root->child_list[i];
            if (!((*found)->props & P_READ_BIT)) {
                return ERROR_MISSING_PERMISSIONS;
        }
                return ERROR_NONE;
            }
        }

        return ERROR_NOT_FOUND;
    }
}

enum error_code pa3_remove_child_from_child_list(struct fs_node *parent, int child_idx) {
    if (parent == NULL || child_idx < 0 || child_idx >= parent->num_children) {
        return ERROR_INVALID_OPERATION;
    }

    // Free the child tree
    pa3_free_tree(parent->child_list[child_idx]);

    // Shift remaining children to fill the gap
    for (int i = child_idx; i < parent->num_children - 1; i++) {
        parent->child_list[i] = parent->child_list[i + 1];
    }

    parent->num_children--;

    // If no more children, free the child_list array
    if (parent->num_children == 0) {
        free(parent->child_list);
        parent->child_list = NULL;
    } else {
        // Optionally: resize the child_list array to save memory
        struct fs_node **new_list = (struct fs_node **)realloc(
            parent->child_list,
            parent->num_children * sizeof(struct fs_node *)
        );

        // Only update the pointer if realloc succeeded
        if (new_list != NULL) {
            parent->child_list = new_list;
        }
    }

    return ERROR_NONE;
}

enum error_code pa3_rm(struct fs_node *root, const char *path, const char *fname) {
    if (strchr(fname, PATH_SEPARATOR)) return ERROR_INVALID_OPERATION;

    // Special case: Prevent removal of the root node
    if ((strcmp(path, "/") == 0 && strcmp(fname, root->name) == 0) ||
        (strcmp(path, "/root") == 0 && strcmp(fname, "root") == 0 && strcmp(root->name, "root") == 0)) {
        return ERROR_INVALID_OPERATION;
    }

    // Find the parent directory
    struct fs_node *parent;
    enum error_code nav_result = pa3_navigate_to_node(root, &parent, path);
    if (nav_result != ERROR_NONE) {
        return nav_result;
    }

    // Check if parent has write permission
    if (!(parent->props & P_WRITE_BIT)) {
        return ERROR_MISSING_PERMISSIONS;
    }

    // Find the child to remove
    int child_idx = -1;
    for (int i = 0; i < parent->num_children; i++) {
        if (strcmp(parent->child_list[i]->name, fname) == 0) {
            child_idx = i;
            break;
        }
    }

    if (child_idx == -1) {
        return ERROR_NOT_FOUND;
    }

    // Additional permission check: if the target is a file, must be writable
    if ((parent->child_list[child_idx]->props & P_FILE_BIT) &&
        !(parent->child_list[child_idx]->props & P_WRITE_BIT)) {
        return ERROR_MISSING_PERMISSIONS;
    }

    // Remove the child
    return pa3_remove_child_from_child_list(parent, child_idx);
}

enum error_code pa3_create_node(struct fs_node *root, const char *path, const char *name, int is_file) {
    if (strchr(name, PATH_SEPARATOR)) {
        return ERROR_INVALID_OPERATION;
    }

    // Find the parent directory
    struct fs_node *parent;
    enum error_code nav_result = pa3_navigate_to_node(root, &parent, path);
    if (nav_result != ERROR_NONE) {
        return nav_result;
    }

// Check if parent is a directory, not a file
if (parent->props & P_FILE_BIT) {
    return ERROR_INVALID_OPERATION;
}

    // Check if parent has write permission
    if (!(parent->props & P_WRITE_BIT)) {
        return ERROR_MISSING_PERMISSIONS;
    }

    // Check if name already exists in parent's children
    for (int i = 0; i < parent->num_children; i++) {
        if (strcmp(parent->child_list[i]->name, name) == 0) {
            return ERROR_ALREADY_EXISTS;
        }
    }

    // Check if parent has reached maximum number of children
    if (parent->num_children >= MAX_NUM_CHILDREN_PER_NODE) {
        return ERROR_INVALID_OPERATION;
    }

    // Create new node
    struct fs_node *new_node = (struct fs_node *)malloc(sizeof(struct fs_node));
    if (new_node == NULL) {
        return ERROR_MALLOC_FAIL;
    }

    // Initialize node properties
    new_node->props = (is_file ? P_FILE_BIT : 0) | P_READ_BIT | P_WRITE_BIT;  // Default: rw permissions
    new_node->num_children = 0;
    new_node->child_list = NULL;

    new_node->name = (char *)malloc(strlen(name) + 1);
    if (new_node->name == NULL) {
        free(new_node);
        return ERROR_MALLOC_FAIL;
    }
    strcpy(new_node->name, name);

    // Add new node to parent's child list
    struct fs_node **new_child_list;
    if (parent->num_children == 0) {
        new_child_list = (struct fs_node **)malloc(sizeof(struct fs_node *));
    } else {
        new_child_list = (struct fs_node **)realloc(
            parent->child_list,
            (parent->num_children + 1) * sizeof(struct fs_node *)
        );
    }

    if (new_child_list == NULL) {
        free(new_node->name);
        free(new_node);
        return ERROR_MALLOC_FAIL;
    }

    parent->child_list = new_child_list;
    parent->child_list[parent->num_children] = new_node;
    parent->num_children++;

    return ERROR_NONE;
}

enum error_code pa3_touch(struct fs_node *root, const char *path, const char *fname) {
    return pa3_create_node(root, path, fname, 1); // 1 means file
}

enum error_code pa3_mkdir(struct fs_node *root, const char *path, const char *dname) {
    return pa3_create_node(root, path, dname, 0); // 0 means directory
}

void pa3_chmod_impl(struct fs_node *node, int read, int write) {
    // Set permissions for current node
    node->props &= ~(P_READ_BIT | P_WRITE_BIT);
    if (read) node->props |= P_READ_BIT;
    if (write) node->props |= P_WRITE_BIT;

    // Apply recursively to all children
    for (int i = 0; i < node->num_children; i++) {
        pa3_chmod_impl(node->child_list[i], read, write);
    }
}
enum error_code pa3_chmod(struct fs_node *root, const char *fpath, int read, int write) {
    struct fs_node *node;
    enum error_code nav_result = pa3_navigate_to_node(root, &node, fpath);
    if (nav_result != ERROR_NONE) return nav_result;
    else if (!(node->props & P_WRITE_BIT)) return ERROR_MISSING_PERMISSIONS;

    pa3_chmod_impl(node, read, write);
    return ERROR_NONE;
}

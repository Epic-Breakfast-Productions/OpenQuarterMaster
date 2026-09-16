#include <linux/init.h>
#include <linux/module.h>
#include <linux/uaccess.h>
#include <linux/fs.h>
#include <linux/proc_fs.h>
#include <linux/slab.h>

// Module metadata
MODULE_AUTHOR("Greg Stewart");
MODULE_DESCRIPTION("Demo driver");
MODULE_LICENSE("GPL");

static struct proc_dir_entry* proc_entry;
static unsigned long numCalls = 0;

static ssize_t custom_read(struct file* file, char __user* user_buffer, size_t count, loff_t* offset){
	if (*offset > 0){
                return 0;
        }

	printk(KERN_INFO "calling our very own custom read method.");
	
	char* output = kasprintf(GFP_KERNEL, "Hello world! Read No: %ld\n", ++numCalls);

	if(!output){
		return -ENOMEM;
	}
	
	int outputLen = strlen(output);
	
	copy_to_user(user_buffer, output, outputLen);
	kfree(output);
	*offset = outputLen;
	return outputLen;
}

static const struct proc_ops proc_ops = {
	.proc_read = custom_read
};


// Custom init and exit methods

static int __init custom_init(void) {
	proc_entry = proc_create("helloworlddriver", 0666, NULL, &proc_ops);
	printk(KERN_INFO "Hello world driver loaded.");
	return 0;
}

static void __exit custom_exit(void) {
	proc_remove(proc_entry);
	printk(KERN_INFO "Goodbye my friend, I shall miss you dearly...");
}

module_init(custom_init);
module_exit(custom_exit);

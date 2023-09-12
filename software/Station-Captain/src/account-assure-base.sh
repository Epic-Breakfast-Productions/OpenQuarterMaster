#!/bin/bash
#echo "got $# args: $@"

HELPTEXT="Usage:"

ARGS_SHORT="hu:p:"
ARGS_LONG="help,username:,password:"
OPTS=$(getopt -a -n account-assure-base.sh --options $ARGS_SHORT --longoptions $ARGS_LONG -- "$@")

VALID_ARGUMENTS=$# # Returns the count of arguments that are in short or long options
if [ "$VALID_ARGUMENTS" -ne 4 ]; then
	echo "$HELPTEXT";
	exit 1;
fi


usernameToAssert=""
passwordToAssert=""

eval set -- "$OPTS"
while :
#echo "switch!"
do
	case "$1" in
		-h | --help)
			echo "$HELPTEXT";
			exit;
		;;
		-u | --username)
			usernameToAssert="$2"
#			echo "got user: $usernameToAssert"
			shift 2
		;;
		-p | --password)
			passwordToAssert="$2"
#			echo "got password: $passwordToAssert"
			shift 2
		;;
		--)
#			echo "done"
			shift;
			break
		;;
	esac
done

if [ -z "$usernameToAssert" ]; then
	echo "No username given."
	exit 2;
fi
if [ -z "$passwordToAssert" ]; then
	echo "No password given."
	exit 2;
fi

#echo "$mode"
#echo "$targetDir"
export usernameToAssert
export passwordToAssert

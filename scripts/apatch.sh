#!/usr/bin/env sh
set -ue

### Disclaimer: This should only be called from the paper tool (either directly
###             or indirectly)

"$basedir"/scripts/requireDeps.sh wiggle

noapply=true
isreject=false
if [ "${1:-}" = "--noapplied" ]; then
    noapply=true
    shift
fi

if [ -n "${1:-}" ]; then
    file="$1"
elif [ -f .git/rebase-apply/patch ]; then
    file=".git/rebase-apply/patch"
    noapply=true
    isreject=true
else
    echo "Please specify a file"
    exit 1
fi

applied=$(echo "$file" | sed 's/.patch$/-applied\.patch/g')
if [ "${1:-}" = "--reset" ]; then
    $gitcmd am --abort
    $gitcmd reset --hard
    $gitcmd clean -f
    exit 0
fi

if [ "$isreject" ] || [ "$($gitcmd am -3 "$file")" ]; then
    echo "Failures - Wiggling"
    $gitcmd reset --hard
    $gitcmd clean -f
    errors=$($gitcmd apply --rej "$file" 2>&1)
    echo "$errors" >> ~/patch.log # FIXME: Why in the wildest unicorn world would we be doing $HOME here?
    export missingfiles=""
    export summaryfail=""
    export summarygood=""
    for i in $(find . -name '*.rej'); do
        base="$(echo "$i" | sed 's/.rej//g')"
        if [ -f "$i" ]; then
            sed -e 's/^diff a\/\(.*\) b\/\(.*\)[[:space:]].*rejected.*$/--- \1\n+++ \2/' -i "$i" && wiggle -v -l --replace "$base" "$i"
            rm -f "$base.porig" "$i"
        else
            echo "No such file: $base"
            missingfiles="$missingfiles\n$base"
        fi
    done
	for i in $($gitcmd status --porcelain | awk '{print $2}'); do
		filedata=$(cat "$i")
		if [ -f "$file" ] && str_contains "$filedata" "<<<<<"; then
			export summaryfail="$summaryfail\nFAILED TO APPLY: $i"
		else
			$gitcmd add "$i"
			export summarygood="$summarygood\nAPPLIED CLEAN: $i"
		fi
	done
	printf '%s' "$summarygood"
	printf '%s' "$summaryfail"
    if str_contains "$errors" "No such file"; then
		echo "===========================";
		echo " "
		echo " MISSING FILES"
	    echo "$errors" | grep "No such file"
		printf '%s' "$missingfiles"
		echo " "
		echo "===========================";
	fi
	$gitcmd status
	$gitcmd diff
fi

if [ ! "$noapply" ]; then
    case "$file" in
        *-applied.patch)
            ;;
        *)
	        mv "$file" "$applied"
            ;;
    esac
fi

# vim: set ff=unix autoindent ts=4 sw=4 tw=0 et :

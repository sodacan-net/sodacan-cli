# Load up some data useful for testing
initialize
mode list
topic list --sort
clock set 2023 02 12 21 19
clock show
module load scc/test31.scc

variable set test31 bb x
variable list test31
topic publish test30 aa aaaaaaaaaaaaaaaa
topic publish test30 ab abababa
topic publish test30 ac acacacacacac
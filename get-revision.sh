#!/bin/bash

  svn info \
| grep "R\?vision" \
| awk '{ print $2 }' \
| head -1

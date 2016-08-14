#!/bin/bash
git fetch github
git rebase github/master
git push -f

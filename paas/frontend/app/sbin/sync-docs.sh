#!/bin/sh

# 获取当前脚本所在目录
script_dir=$(dirname "$0")

# 找到 "../../docs" 这个文档目录
docs_dir="$script_dir/../../docs"

# 先删除文档目录里面的所有内容，包括隐藏文件和文件夹
rm -rf "$docs_dir"

# clone项目的frontend分支到文档目录，只克隆最近一次提交的内容
git clone --depth=1 --single-branch -b frontend https://github.com/alibaba-open-source-website/sreworks.git "$docs_dir"

# 删除文档目录下的 .git 目录，避免影响父项目的git元信息
rm -rf "$docs_dir/.git"
rm -rf "$docs_dir/.github"

## xfire
基于Ｏpenfire的一个即时通讯工具。

#. 更新最近源码：

(1)原则是，通过插件来增加新功能，不修改和性源码。

(2)将源码用ant编译出一个最新可执行版本，替换掉bundle的openfire。删除openfire\lib下的jar，删除admin插件的admin.jar

(3)拷贝最新的java源文件替换core功能下的java代码，拷贝最新的jsp文件替换掉core下的jsp文件。

(4)由于官方工程jar会升级，需要根据官方工程中的bulid\lib\versions.txt中描述的jar版本，调整parent中相应的jar

# 本工程的编译工程：

cd  xfire

sh package-dev.sh

cd bundle

sh package.sh

在bundle下会生成可运行的openfire

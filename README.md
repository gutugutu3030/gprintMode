# gprintMode
Processing 2.x用のコンソールを強化するモードです．コンソールからソースを検索したり，printlnからコンソールを検索したり出来ます．
## Download
[こちらからダウンロードできます](https://www.dropbox.com/s/5ogamroq70ywa4n/GreatPrintMode.zip?dl=0)
## 導入方法
使用するには[oscP5](http://www.sojamo.de/libraries/oscP5/)を用意する必要があります．
解凍してできたGPringModeフォルダを，processingフォルダ(Preferences→sketch locationで確認できます)の中のmodesフォルダに入れます．
Processingを再起動すると，mode選択のプルダウンにGPrintが出現するはずです．
## 仕様
#### 操作方法
コンソールの記述をクリックすると，それが描画されたprintln命令をハイライトしてくれます．
逆に，プログラム内のprintln命令のある行をクリックするとその命令によって出力された文章を全てハイライトします．

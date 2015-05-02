# -*- coding: utf-8 -*-
# collect.rb        
# 内容 :フォルダ内のファイル名を読み込み、そのすべてのデータを１つにまとめる
# Copyright (c) 2002 Mitsuo Minagawa, All rights reserved.
# (minagawa@fb3.so-net.ne.jp)          
# 使用方法 : c:\>ruby collect.rb    
#   
in1_file    =   open("logNo1.txt","r")    
out1_file   =   open("output.txt","w")  
while   (filename   =   in1_file.gets)  
    filename.chomp! 
    in2_file    =   open(filename,"r")  
    while   (line1  =   in2_file.gets)  
        out1_file.print line1 
    end 
end 

in1_file.close  
in2_file.close  
out1_file.close 
   

# CityPickerDemo
城市选择器

对城市选取器的进一步封装
配置很简单，只需如下几步： 

   CityPickerPopWindow mPopWindow = new CityPickerPopWindow(mContext);  
   
   mPopWindow.showPopupWindow(rootView);//设置popwindow显示位置 
   
   mPopWindow.setCityPickListener(this);//设置城市选择监听  


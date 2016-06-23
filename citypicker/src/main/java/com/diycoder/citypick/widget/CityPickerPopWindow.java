package com.diycoder.citypick.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.diycoder.citypick.OnWheelChangedListener;
import com.diycoder.citypick.R;
import com.diycoder.citypick.WheelView;
import com.diycoder.citypick.adapters.ArrayWheelAdapter;
import com.diycoder.citypick.model.CityModel;
import com.diycoder.citypick.model.DistrictModel;
import com.diycoder.citypick.model.ProvinceModel;
import com.diycoder.citypick.service.XmlParserHandler;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by diycoder on 16/6/23.
 */
public class CityPickerPopWindow extends PopupWindow implements OnWheelChangedListener {

    /**
     */
    protected String[] mProvinceDatas;
    /**
     */
    protected Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
    /**
     */
    protected Map<String, String[]> mDistrictDatasMap = new HashMap<String, String[]>();

    /**
     */
    protected Map<String, String> mZipcodeDatasMap = new HashMap<String, String>();

    /**
     */
    protected String mCurrentProviceName;
    /**
     */
    protected String mCurrentCityName;
    /**
     */
    protected String mCurrentDistrictName = "";

    /**
     */
    protected String mCurrentZipCode = "";

    /**
     */
    private String divider = "-";
    /**
     */
    private String pickValue = "";

    /**
     */

    private Activity mContext;
    private WheelView mViewProvince;
    private WheelView mViewCity;
    private WheelView mViewDistrict;
    private final View cityPickerView;
    private final LayoutInflater inflater;
    private final Display display;
    private final WindowManager windowManager;
    private Button closePopWindow;
    private Button showValue;
    private CityPickListener cityPickListner;

    public CityPickerPopWindow(Activity context) {
        super(context);
        this.mContext = context;

        initProvinceDatas();

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cityPickerView = inflater.inflate(R.layout.popwindow_city_picker, null);

        //设置CityPickerPopWindow的View
        this.setContentView(cityPickerView);
        //设置CityPickerPopWindow弹出窗体的宽

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();

        this.setWidth(RelativeLayout.LayoutParams.FILL_PARENT);
        //设置CityPickerPopWindow弹出窗体的高
        this.setHeight((int) (display.getHeight() * 0.3));
        //设置CityPickerPopWindow弹出窗体可点击
        this.setFocusable(true);
        //设置CityPickerPopWindow可触摸
        this.setTouchable(true);
        //设置CityPickerPopWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimBottom);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xFFFFFFFF);
        //设置CityPickerPopWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        this.setOutsideTouchable(false);
        setUpListener(cityPickerView);
        setUpData();

    }

    /**
     * 显示popupWindow
     *
     * @param parent
     */
    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            this.showAtLocation(parent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
        } else {
            this.dismiss();
        }
    }

    /**
     * 设置地区选择器监听器
     *
     * @param cityPickerView
     */
    private void setUpListener(View cityPickerView) {
        closePopWindow = (Button) cityPickerView.findViewById(R.id.closePop);
        showValue = (Button) cityPickerView.findViewById(R.id.showValue);


        mViewProvince = (WheelView) cityPickerView.findViewById(R.id.id_province);
        mViewCity = (WheelView) cityPickerView.findViewById(R.id.id_city);
        mViewDistrict = (WheelView) cityPickerView.findViewById(R.id.id_district);
        mViewProvince.addChangingListener(this);
        mViewCity.addChangingListener(this);
        mViewDistrict.addChangingListener(this);

        showValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cityPickListner != null) {
                    cityPickListner.pickValue(pickValue);
                }
                dismiss();
            }
        });
        closePopWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    /**
     * 初始化地区选择器数据
     */
    private void setUpData() {
        mViewProvince.setViewAdapter(new ArrayWheelAdapter<String>(mContext, mProvinceDatas));
        mViewProvince.setVisibleItems(7);
        mViewCity.setVisibleItems(7);
        mViewDistrict.setVisibleItems(7);
        updateCities();
        updateAreas();
    }

    /**
     * 更新城市选择器数据
     */
    private void updateAreas() {
        int pCurrent = mViewCity.getCurrentItem();
        mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[pCurrent];
        String[] areas = mDistrictDatasMap.get(mCurrentCityName);

        if (areas == null) {
            areas = new String[]{""};
        }
        mViewDistrict.setViewAdapter(new ArrayWheelAdapter<String>(mContext, areas));
        mViewDistrict.setCurrentItem(0);
    }

    /**
     * 更新城市
     */
    private void updateCities() {
        int pCurrent = mViewProvince.getCurrentItem();
        mCurrentProviceName = mProvinceDatas[pCurrent];
        String[] cities = mCitisDatasMap.get(mCurrentProviceName);
        if (cities == null) {
            cities = new String[]{""};
        }
        mViewCity.setViewAdapter(new ArrayWheelAdapter<String>(mContext, cities));
        mViewCity.setCurrentItem(0);

        updateAreas();
    }


    /**
     * 初始化数据
     */
    protected void initProvinceDatas() {
        List<ProvinceModel> provinceList = null;
        AssetManager asset = mContext.getAssets();
        try {
            InputStream input = asset.open("province_data.xml");
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser parser = spf.newSAXParser();
            XmlParserHandler handler = new XmlParserHandler();
            parser.parse(input, handler);
            input.close();
            provinceList = handler.getDataList();
            if (provinceList != null && !provinceList.isEmpty()) {
                mCurrentProviceName = provinceList.get(0).getName();
                List<CityModel> cityList = provinceList.get(0).getCityList();
                if (cityList != null && !cityList.isEmpty()) {
                    mCurrentCityName = cityList.get(0).getName();
                    List<DistrictModel> districtList = cityList.get(0).getDistrictList();
                    mCurrentDistrictName = districtList.get(0).getName();
                    mCurrentZipCode = districtList.get(0).getZipcode();
                }
            }
            //*/
            mProvinceDatas = new String[provinceList.size()];
            for (int i = 0; i < provinceList.size(); i++) {
                mProvinceDatas[i] = provinceList.get(i).getName();
                List<CityModel> cityList = provinceList.get(i).getCityList();
                String[] cityNames = new String[cityList.size()];
                for (int j = 0; j < cityList.size(); j++) {
                    cityNames[j] = cityList.get(j).getName();
                    List<DistrictModel> districtList = cityList.get(j).getDistrictList();
                    String[] distrinctNameArray = new String[districtList.size()];
                    DistrictModel[] distrinctArray = new DistrictModel[districtList.size()];
                    for (int k = 0; k < districtList.size(); k++) {
                        DistrictModel districtModel = new DistrictModel(districtList.get(k).getName(), districtList.get(k).getZipcode());
                        mZipcodeDatasMap.put(districtList.get(k).getName(), districtList.get(k).getZipcode());
                        distrinctArray[k] = districtModel;
                        distrinctNameArray[k] = districtModel.getName();
                    }
                    mDistrictDatasMap.put(cityNames[j], distrinctNameArray);
                }
                mCitisDatasMap.put(provinceList.get(i).getName(), cityNames);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {

        }
    }

    /**
     * 滚动监听
     *
     * @param wheel    the wheel view whose state has changed
     * @param oldValue the old value of current item
     * @param newValue the new value of current item
     */

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (wheel == mViewProvince) {
            updateCities();
        } else if (wheel == mViewCity) {
            updateAreas();
        } else if (wheel == mViewDistrict) {
            mCurrentDistrictName = mDistrictDatasMap.get(mCurrentCityName)[newValue];
            mCurrentZipCode = mZipcodeDatasMap.get(mCurrentDistrictName);
        }
        pickValue = mCurrentProviceName + divider + mCurrentCityName + divider + mCurrentDistrictName;
    }

    public void setDivider(String divider) {
        this.divider = divider;
    }

    public interface CityPickListener {
        void pickValue(String value);
    }

    public void setCityPickListener(CityPickListener listener) {
        this.cityPickListner = listener;
    }

}

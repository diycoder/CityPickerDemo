package com.diycoder.citypick.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

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
 * Created by lq on 16/6/23.
 */
public class CityPickerDialog extends Dialog {

    private Context mContext;


    public CityPickerDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    public static class Builder implements OnWheelChangedListener {

        private WheelView mViewProvince;
        private WheelView mViewCity;
        private WheelView mViewDistrict;
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
        private CityPickListener cityPickListner;


        private Context context;
        private String title;
        private String message;
        private View contentView;
        private String positiveButtonText;
        private String negativeButtonText;
        private DialogInterface.OnClickListener positiveClickListener;
        private DialogInterface.OnClickListener negativeClickListener;
        private Button cancelView;
        private Button surelView;
        private View layout;

        public Builder(Context context) {
            this.context = context;
            initProvinceDatas();
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }


        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }


        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context.getText(positiveButtonText);
            this.positiveClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context.getText(negativeButtonText);
            this.negativeClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeClickListener = listener;
            return this;
        }

        public View getLayout() {
            return layout;
        }

        public CityPickerDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CityPickerDialog dialog = new CityPickerDialog(context);
            layout = inflater.inflate(R.layout.dialog_city_picker, null);

            setUpData();
            setUpListener(layout);

            dialog.addContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            cancelView = (Button) layout.findViewById(R.id.cancel);
            surelView = (Button) layout.findViewById(R.id.sure);
            cancelView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (negativeClickListener != null) {
                        negativeClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                    }
                }
            });
            surelView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (negativeClickListener != null) {
                        negativeClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                    }
                }
            });
            dialog.setContentView(layout);

            return dialog;
        }

        /**
         * 设置地区选择器监听器
         *
         * @param cityPickerView
         */
        public void setUpListener(View cityPickerView) {

            mViewProvince = (WheelView) cityPickerView.findViewById(R.id.id_province);
            mViewCity = (WheelView) cityPickerView.findViewById(R.id.id_city);
            mViewDistrict = (WheelView) cityPickerView.findViewById(R.id.id_district);
            mViewProvince.addChangingListener(this);
            mViewCity.addChangingListener(this);
            mViewDistrict.addChangingListener(this);

        }

        /**
         * 初始化地区选择器数据
         */
        public void setUpData() {
            mViewProvince.setViewAdapter(new ArrayWheelAdapter<String>(context, mProvinceDatas));
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
            mViewDistrict.setViewAdapter(new ArrayWheelAdapter<String>(context, areas));
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
            mViewCity.setViewAdapter(new ArrayWheelAdapter<String>(context, cities));
            mViewCity.setCurrentItem(0);
            updateAreas();
        }


        /**
         * 初始化数据
         */
        protected void initProvinceDatas() {
            List<ProvinceModel> provinceList = null;
            AssetManager asset = context.getAssets();
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
            if (cityPickListner != null) {
                cityPickListner.pickValue(pickValue);
            }
        }

        public interface CityPickListener {
            void pickValue(String value);
        }

        public void setCityPickListener(CityPickListener listener) {
            this.cityPickListner = listener;
        }

    }

}

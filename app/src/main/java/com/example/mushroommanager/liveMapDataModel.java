package com.example.mushroommanager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class liveMapDataModel extends ViewModel {

    private final MutableLiveData<String> liveMapData = new MutableLiveData<String>();

    public  void setData(String data){
        liveMapData.setValue(data);
    }
    public LiveData<String> getLiveMapData(){

        return liveMapData;
    }

}

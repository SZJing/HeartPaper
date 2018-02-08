package com.example.heartpaper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.heartpaper.bean.Water;

import java.util.ArrayList;
import java.util.List;

public class WaterActivity extends AppCompatActivity {
    private RecyclerView recy;
    private Water[] water = {new Water(R.mipmap.heart_icon,"爱你红心"),new Water(R.mipmap.flower_icon,"青春小花"),new Water(R.mipmap.star_icon,"满天星星")
    ,new Water(R.mipmap.kitty_icon,"小小黑猫"),new Water(R.mipmap.snowflake_icon,"冬日冰花"),new Water(R.mipmap.china_icon,"爱我中华")
    ,new Water(R.mipmap.checkn_icon,"小鸡快跑"),new Water(R.mipmap.i_love_icon,"I Love You"),new Water(R.mipmap.butterfly_icon,"美丽蝴蝶")
    ,new Water(R.mipmap.hello_kitty_icon,"Hello Kitty"),new Water(R.mipmap.shark_icon,"大鲨鱼")};
    private List<Water> list = new ArrayList<>();
    private WaterAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water);
        initWater();
        recy = (RecyclerView) findViewById(R.id.recycler);
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        recy.setLayoutManager(layoutManager);
        adapter = new WaterAdapter(list);
        recy.setAdapter(adapter);
        adapter.setOnClickWater(new WaterAdapter.onClickWater() {
            @Override
            public void WaterClick(int id) {
                Water water = list.get(id);
                Intent intent = new Intent();
                intent.putExtra("WaterId",String.valueOf(water.getImageId()));
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

    private void initWater(){
        for (int i = 0; i<water.length; i++){
            list.add(water[i]);
        }
    }
}

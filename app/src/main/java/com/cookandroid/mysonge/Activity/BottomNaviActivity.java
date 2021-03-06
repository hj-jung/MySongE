package com.cookandroid.mysonge.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cookandroid.mysonge.DTO.Schedule;
import com.cookandroid.mysonge.Fragment.DailyFragment;
import com.cookandroid.mysonge.Fragment.MainFragment;
import com.cookandroid.mysonge.Fragment.MypageFragment;
import com.cookandroid.mysonge.Fragment.RoutineFragment;
import com.cookandroid.mysonge.Fragment.TimelineFragment;
import com.cookandroid.mysonge.Interface.CharacterService;
import com.cookandroid.mysonge.Interface.ScheduleService;
import com.cookandroid.mysonge.Retrofit.RetrofitClient;
import com.cookandroid.mysonge.Interface.RoutineService;
import com.cookandroid.mysonge.R;
import com.cookandroid.mysonge.DTO.Character;
import com.cookandroid.mysonge.DTO.Routine;
import com.cookandroid.mysonge.Util.RoutineData;
import com.cookandroid.mysonge.Util.ScheduleData;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomNaviActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView; //바텀 네비게이션 뷰
    private FragmentManager fm = getSupportFragmentManager(); //fragmentManager생성

    private CharacterService characterService;
    private RoutineService routineService;

    private int characterid, user_id;

    private String characterName, characterQuote;

    private String routineAchive = "NaN";

    private int time;
    private String color, title, startH, AMPM, strLocalTime;

    Bundle myPagebundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottomnavi);

        Intent intent = getIntent(); //전달할 데이터를 받을 Intent
        // text 키값으로 데이터를 받는다. String을 받아야 하므로 getStringExtra()를 사용함

        if (intent != null) {
            characterid = intent.getIntExtra("characterID", 0);
            user_id = intent.getIntExtra("userID", -1);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("userID", user_id);
        MainFragment mainFragment = new MainFragment();
        mainFragment.setArguments(bundle);

        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        fm.beginTransaction().add(R.id.btmnavi_frame, mainFragment).commit(); //첫 fragment 화면 설정

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction ft = fm.beginTransaction(); //실제 transaction 실행하는 아이-얘가 onNavigationItemSelected밖에 있으면 'commit already called'오류
                switch (item.getItemId()){  //fragment 교체 실행 함수
                    case R.id.tab_main:
                        Bundle bundle = new Bundle();
                        bundle.putInt("userID", user_id);
                        MainFragment mainFragment = new MainFragment();
                        mainFragment.setArguments(bundle);
                        ft.replace(R.id.btmnavi_frame, mainFragment).commit(); break;
                    case R.id.tab_daily:
                        getDailySchedule();
                        Bundle dailyBundle = new Bundle();
                        dailyBundle.putInt("userID", user_id);
                        DailyFragment dailyFragment = new DailyFragment();
                        dailyFragment.setArguments(dailyBundle);
                        ft.replace(R.id.btmnavi_frame, dailyFragment).commit(); break;
                    case R.id.tab_routine:
                        getRoutine();
                        ft.replace(R.id.btmnavi_frame, new RoutineFragment()).commit(); break;
                    case R.id.tab_mypage:
                        getCharacter();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getAchieve();
                        ft.replace(R.id.btmnavi_frame, new MypageFragment()).commit(); break;
                }
                return true;
            }
        });

        characterService = RetrofitClient.getClient().create(CharacterService.class);
        routineService = RetrofitClient.getClient().create(RoutineService.class);
    }

    private void getCharacter() {
        Call<Character> call = characterService.getCharacter(user_id);
        call.enqueue(new Callback<Character>() {
            @Override
            public void onResponse(Call<Character> call, Response<Character> response) {
                if (!response.isSuccessful()) {
                    Log.d("MainActivity", String.valueOf(response.code()));
                    return;
                }

                Character characterResponse = response.body();

                Log.d("TAG", characterResponse.getName());

                characterName = characterResponse.getName();
                characterQuote = characterResponse.getQuote();

                myPagebundle.putInt("id",characterResponse.getId());
                myPagebundle.putString("name", characterName);
                myPagebundle.putString("message", characterQuote);
                myPagebundle.putInt("userID", user_id);
            }
            @Override
            public void onFailure(Call<Character> call, Throwable t) {
                Log.d("getCharacter=", t.getMessage());
            }
        });
    }

    private void getRoutine() {

        Call<List<Routine>> call = routineService.getRoutine(user_id);
        call.enqueue(new Callback<List<Routine>>() {
            @Override
            public void onResponse(Call<List<Routine>> call, Response<List<Routine>> response) {
                if (!response.isSuccessful()) {
                    Log.d("MainActivity", String.valueOf(response.code()));
                    return;
                }
                List<Routine> routineList = response.body();

                Bundle bundle = new Bundle();
                ArrayList<RoutineData> routineDataArrayList = new ArrayList<RoutineData>();

                for (Routine routine : routineList ) {
                    System.out.println(routine.getId());
                    String routineStrTime = routine.getRoutineTime();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                        LocalTime localTime= LocalTime.parse(routineStrTime, formatter);
                        strLocalTime = localTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH));
                        System.out.println(strLocalTime);
                    }

                    routineDataArrayList.add(new RoutineData(routine.getId(), routine.getName(), strLocalTime, routine.getContext(), routine.getAchieve()));
                }

                bundle.putParcelableArrayList("routineList", (ArrayList<? extends Parcelable>) routineDataArrayList);
                bundle.putInt("userID", user_id);

                if (routineDataArrayList.isEmpty()) System.out.println("routineDataArrayList is empty");
                else {
                    for (RoutineData routineData : routineDataArrayList) {
                        System.out.println(routineData.routineName);
                    }
                }

                RoutineFragment routineFragment = new RoutineFragment();
                routineFragment.setArguments(bundle);
                fm.beginTransaction().replace(R.id.btmnavi_frame, routineFragment).commit();
            }

            @Override
            public void onFailure(Call<List<Routine>> call, Throwable t) {
                Log.d("getCharacter=", t.getMessage());
            }
        });
    }

    private void getDailySchedule() {

        //오늘 날짜 일정 GET
        ScheduleService scheduleService = RetrofitClient.getClient().create(ScheduleService.class);
        Call<List<Schedule>> call = scheduleService.today(user_id);
        call.enqueue(new Callback<List<Schedule>>() {
            @Override
            public void onResponse(Call<List<Schedule>> call, Response<List<Schedule>> response) {
                if(!response.isSuccessful()){
                    Log.d("TAG", String.valueOf(response.code()));
                    return;
                }
                Log.d("TAG", "Schedule Response Success");
                List<Schedule> scheduleResponse = response.body();

                Bundle bundle = new Bundle();
                ArrayList<ScheduleData> scheduleData = new ArrayList<>();

                for(Schedule schedule : scheduleResponse){
                    color = schedule.getColor();
                    title = schedule.getTitle();
                    String startTime = schedule.getStartHms();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                        LocalTime localTime= LocalTime.parse(startTime, formatter);
                        time = Integer.valueOf(localTime.getHour());
                        if(localTime.getHour()>12){
                            startH = String.format("%02d",(localTime.getHour()-12));
                            AMPM = "PM";
                        }
                        else{
                            startH = String.format("%02d",(localTime.getHour()));
                            AMPM = "AM";
                        }
                    }
                    scheduleData.add(new ScheduleData(schedule.getColor(), schedule.getContext(), schedule.getEndHms(), schedule.getLocation(), schedule.getStartHms(), schedule.getStartYmd(), startH, schedule.getTitle(), time, AMPM, schedule.getX(), schedule.getY()));
                }

                bundle.putParcelableArrayList("TodaySchedule", (ArrayList<? extends Parcelable>) scheduleData);

                TimelineFragment timelineFragment = new TimelineFragment();
                timelineFragment.setArguments(bundle);
                fm.beginTransaction().replace(R.id.timelineView, timelineFragment).commit();
            }

            @Override
            public void onFailure(Call<List<Schedule>> call, Throwable t) {
                Log.d("TAG", t.getMessage());
            }
        });
    }

    private void getAchieve() {
        Call<String> call = routineService.getAchieve(user_id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful()) {
                    Log.d("TAG", String.valueOf(response.code()));
                    return;
                }
                routineAchive = response.body();
                myPagebundle.putString("routineAchive", routineAchive);
                MypageFragment mypageFragment = new MypageFragment();
                mypageFragment.setArguments(myPagebundle);

                fm.beginTransaction().replace(R.id.btmnavi_frame, mypageFragment).commit();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("TAG", t.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("tag", "===requestCode===" + requestCode);
        if (requestCode == 2) {
            if (data != null) {
                String name = data.getStringExtra("name");
                String message = data.getStringExtra("message");

                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("message", message);

                MypageFragment mypageFragment = new MypageFragment();
                mypageFragment.setArguments(bundle);
                fm.beginTransaction().replace(R.id.btmnavi_frame, mypageFragment).commit();
            }
        }

        if (requestCode == 3) {
            getRoutine();
            RoutineFragment routineFragment = new RoutineFragment();
            fm.beginTransaction().replace(R.id.btmnavi_frame, routineFragment).commit();
        }
        if (requestCode == 4) {
            getRoutine();
            RoutineFragment routineFragment = new RoutineFragment();
            fm.beginTransaction().replace(R.id.btmnavi_frame, routineFragment).commit();
        }
    }
}


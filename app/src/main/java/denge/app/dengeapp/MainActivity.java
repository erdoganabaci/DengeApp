package denge.app.dengeapp;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar görünmesi için ama app bar layout kullandığımdan gerek kalmadı
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar1);
        //toolbar.setTitle("degismiyor :(");
        this.setSupportActionBar(toolbar);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        //açılışta home fragmenti gösteriyor
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new SingleFootFragment()).commit();
    }
    //fazla kod olasın diye create altına yazmadım
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;
                    //menu itemlara göre fragmentin layotunu getiriyoruz
                    switch (menuItem.getItemId()){
                        case R.id.nav_singlefoot:
                            selectedFragment = new SingleFootFragment();
                            break;
                        case R.id.nav_multifoot:
                            selectedFragment = new MultiFootFragment();
                            break;
                        case R.id.nav_plank:
                            selectedFragment = new PlankFragment();
                            break;
                         case R.id.nav_bluetooth:
                            selectedFragment = new BluetoothFragment();
                            break;
                    }
                    //main layoutta o fragment yerine hangi fragment geçsin ona karar veriyoruz
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
                    //seçilen fragmenti getir.
                    return true;
                }
            };

    public void infoPrinter(View view){
        Toast.makeText(this,"Print butonuna tıkladınız",Toast.LENGTH_LONG).show();
    }
    public void toolbarInfoPrinter(View view){
        Toast.makeText(this,"Toolbar Print butonuna tıkladınız",Toast.LENGTH_LONG).show();

    }
}

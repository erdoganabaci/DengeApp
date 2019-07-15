package denge.app.dengeapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothFragment extends Fragment {
    BluetoothAdapter myBluetooth;
    //eşleşmiş cihazları gösteriyoruz.etraftaki cihazları atıyoruz.
    private Set<BluetoothDevice> pairedDevices;
    Button toggleButton;
    Button pairedButton;

    ListView pairList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView =  inflater.inflate(R.layout.fragment_bluetooth,container,false);
        //fragment üzerinden çağırmak gerekiyor.
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        toggleButton = fragmentView.findViewById(R.id.toggleButton);
        pairedButton = fragmentView.findViewById(R.id.pairedButton);
        pairList = fragmentView.findViewById(R.id.pairedList);

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toogleBluetooth();
            }
        });
        pairedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listDevice();

            }
        });
        return fragmentView;
    }
    /*public void pairedButton(View view){
        listDevice();
    }
    public void toogleButton(View view){
        toogleBluetooth();
    }*/
    private void toogleBluetooth() {
        //eğer bluetooth adaptörüm yoksa
        if (myBluetooth == null){
            Toast.makeText(getActivity(),"You Have No Bluetooth Device",Toast.LENGTH_LONG).show();
        }
        //eğer bluettoth adaptörüm açık değilse açtırcaz
        if (!myBluetooth.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            toggleButton.setText("BT DEACTİVE");

        }
        //eğer bluetooth aktifse kapatıyoruz.
        if (myBluetooth.isEnabled()){
            myBluetooth.disable();
            toggleButton.setText("BT ACTİVE");


        }
    }

    private void listDevice() {
        //eşleşmiş cihazları listenin içine koyduk 1 sefer göstercek setin özelliği küme gibi :)
        pairedDevices = myBluetooth.getBondedDevices();
        //listview e eklicez
        ArrayList list = new ArrayList();
        //eğer eşleşmiş cihaz varsa bulduğu eşleşmiş cihazları listeye eklicez.
        if (pairedDevices.size() >0){
            for (BluetoothDevice bt : pairedDevices){
                list.add(bt.getName() +"\n"+bt.getAddress());
                //list.add(bt.getAddress());
                //Toast.makeText(getApplicationContext(),bt.getName().toString(),Toast.LENGTH_SHORT).show();

            }

        }
        else{
            Toast.makeText(getActivity(),"No Paired Devices",Toast.LENGTH_LONG).show();

        }
        final ArrayAdapter adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1,list);
        pairList.setAdapter(adapter);
        //listede değişiklik olursa güncelle
        adapter.notifyDataSetChanged();
    }

}

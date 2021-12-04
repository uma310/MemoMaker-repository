package com.example.memomaker;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment {

    public int i;
    public interface ParentFragmentListener {
        void onClickButton(int position, String text);
    }

    private ParentFragmentListener listener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        //context を listener に代入
        if (context instanceof ParentFragmentListener) {
            listener = (ParentFragmentListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search, container, false);
        return view;

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // ListViewに表示するデータ
        ArrayList<String> items = getArguments().getStringArrayList("text");
        ArrayList<String> subItems = getArguments().getStringArrayList("date");
        //items.add("データ1");
        //items.add("データ2");
        //items.add("データ3");
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();

        for (i=0; i<items.size(); i++){
            Map<String, String> item = new HashMap<String, String>();
            item.put("text", items.get(i));
            item.put("date", subItems.get(i));
            data.add(item);
        }
        TextView textView = view.findViewById(R.id.date_text);
        String str = Integer.valueOf(i).toString();
        str += "個見つかりました";
        textView.setText(str);

        // ListViewをセット
        SimpleAdapter adapter = new SimpleAdapter(this.getContext(), data, android.R.layout.simple_list_item_2, new String[] { "text", "date" },
                new int[] { android.R.id.text1, android.R.id.text2});
        ListView listView = (ListView) view.findViewById(R.id.date_search);
        listView.setAdapter(adapter);

        // セルを選択されたら詳細画面フラグメント呼び出す
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                // 詳細画面へ値を渡す
                String text = items.get(position);
                if(listener != null){
                    listener.onClickButton(position, text);
                }
                getFragmentManager().beginTransaction().remove(SearchFragment.this).commit();
//                DetailFragment fragment = new DetailFragment();
//                Bundle bundle = new Bundle();
//                bundle.putInt("selected",position);
//                fragment.setArguments(bundle);
//                // 詳細画面を呼び出す
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                FragmentTransaction transaction = fragmentManager.beginTransaction();
//                transaction.replace(R.id.main_fragment, fragment);
//                // 戻るボタンで戻ってこれるように
//                transaction.addToBackStack(null);
//                transaction.commit();
            }
        });
    }
}

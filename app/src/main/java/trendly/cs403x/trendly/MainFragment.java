package trendly.cs403x.trendly;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {
    private TabLayout homeTabBar;
    private ViewPager homeViewPager;

    private Fragment pantsFragment;
    private Fragment shirtsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        homeTabBar = (TabLayout) view.findViewById(R.id.homeTabBar);
        homeViewPager = (ViewPager) view.findViewById(R.id.homeViewPager);

        setUpTabBar();
        setUpViewPager();

        homeTabBar.setTabGravity(TabLayout.GRAVITY_FILL);
        homeTabBar.setupWithViewPager(homeViewPager);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void setUpTabBar() {
        pantsFragment = new BlankFragment();
        shirtsFragment = new BlankFragment();
        TabLayout.Tab pantsTab = homeTabBar.newTab();
        TabLayout.Tab shirtsTab = homeTabBar.newTab();

        homeTabBar.addTab(pantsTab);
        homeTabBar.addTab(shirtsTab);
    }

    private void setUpViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(pantsFragment, "Pants");
        viewPagerAdapter.addFragment(shirtsFragment, "Shirts");
        homeViewPager.setAdapter(viewPagerAdapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}

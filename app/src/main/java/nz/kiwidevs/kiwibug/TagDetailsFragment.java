package nz.kiwidevs.kiwibug;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import nz.kiwidevs.kiwibug.utils.ReverseGeocodingTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TagDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TagDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagDetailsFragment extends android.support.v4.app.Fragment implements ReverseGeocodeCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Context context;
    private TagDetailsFragment tagDetailsFragment;

    private OnFragmentInteractionListener mListener;

    private String tagIdentifier;

    private ProgressDialog progressDialog;

    private GoogleMap googleMap;

    private int polylineColour = Color.BLUE;

    private ListView listViewLocationHistory;

    private TagRecordListViewAdapter adapter;
    private  ArrayList<TagRecord> tagRecordArrayList = new ArrayList<>();


    public TagDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TagDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TagDetailsFragment newInstance(String param1, String param2) {
        TagDetailsFragment fragment = new TagDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tag_details, container, false);

        getActivity().setTitle("Tag Details");

        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("nz.kiwidevs.kiwibug.SHARED", Context.MODE_PRIVATE);
        boolean showTutorial = sharedPreferences.getBoolean("show_tag_details_tutorial", true);

        if(showTutorial){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Tag Details");
            builder.setMessage("To jump to a particular point in the tag's path/route, just tap a location in the list at the bottom of the screen, and the map will automatically " +
                    "zoom in on that point");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("show_tag_details_tutorial", false);
                    editor.commit();
                }
            });

            builder.show();
        }

        tagDetailsFragment = this;

        Bundle bundle = getArguments();
        tagIdentifier = bundle.getString("Tag ID");

        MapView mapView = (MapView) view.findViewById(R.id.mapViewTagRoute);
        listViewLocationHistory = (ListView) view.findViewById(R.id.listViewTagLocationHistory);

        listViewLocationHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TagRecord tagRecord = (TagRecord) listViewLocationHistory.getItemAtPosition(position);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(tagRecord.getLatitude(), tagRecord.getLongitude()), 15);
                googleMap.animateCamera(cameraUpdate);
            }
        });

        setAdapter();


        mapView.onCreate(savedInstanceState);

        mapView.onResume();

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap map) {
                googleMap = map;


                //Lets use the approximate center of NZ and then zoom in to the users location
                LatLng nelson = new LatLng(-41.270632, 173.283965);


                CameraPosition cameraPosition = new CameraPosition.Builder().target(nelson).zoom(5).build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));



            }
        });


        //@TODO: Pass TagID to TagDetailsFragment and replace ID in URL
        Ion.with(this)
                .load("http://netweb.bplaced.net/kiwibug/api.php?action=getTagData&id=" + tagIdentifier)
                .as(new TypeToken<TagRecord[]>(){})
                .setCallback(new FutureCallback<TagRecord[]>() {
                    @Override
                    public void onCompleted(Exception e, TagRecord[] tagRecordArray) {
                        if(progressDialog != null && progressDialog.isShowing()){
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        PolylineOptions poly = new PolylineOptions().color(polylineColour).width(5);

                        for(TagRecord tagRecord : tagRecordArray){
                            tagRecordArrayList.add(tagRecord);

                            LatLng currentMarkerLatLng = new LatLng(tagRecord.getLatitude(),tagRecord.getLongitude());
                            googleMap.addMarker(new MarkerOptions().position(currentMarkerLatLng).title(tagRecord.getID() + " " + tagRecord.getTagID()).icon(BitmapDescriptorFactory
                                    .defaultMarker(273)));

                            //Log.d("TagDetails",tagRecord.getID() + " " + tagRecord.getTagTime());

                            poly.add(currentMarkerLatLng);

                            //Async gecoding task here
                            new ReverseGeocodingTask(context, tagRecord, tagDetailsFragment).execute(currentMarkerLatLng);



                        }




                        googleMap.addPolyline(poly);


                    }
                });






        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void setAdapter(){
        adapter = new TagRecordListViewAdapter(context, R.layout.tagrecord_row, tagRecordArrayList);
        listViewLocationHistory.setAdapter(adapter);
    }






    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onReverseGeocodeComplete() {
        adapter.refreshAdapter(tagRecordArrayList);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }




}

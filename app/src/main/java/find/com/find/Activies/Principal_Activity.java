package find.com.find.Activies;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

import find.com.find.Fragments.Register_Map_Fragmento;
import find.com.find.Model.UsuarioAtivoSingleton;
import find.com.find.R;
import find.com.find.Util.PermissionUtils;

public class Principal_Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private NavigationView navigationView;
    private CoordinatorLayout mCoordinatorLayout;
    private Button btn_Header_Login;
    private Button btn_Header_Cadastro;
    private static final String TAG = Principal_Activity.class.getSimpleName();
    private GoogleApiClient googleApiClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECAR_GPS = 2;
    private static final int REQUEST_ERRO_PLAY_SERVICES = 1;
    private static final String EXTRA_DIALOG = "dialog";
    private boolean mDeveExibirDialog, flagEnableMap = false;
    int mTentativas;
    Handler mHandler;
    GoogleMap mMap;
    LatLng mOrigem;
    public static Location localizacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationMenu();
        btn_Header_Login = (Button) findViewById(R.id.header_btnLogin);

        btn_Header_Cadastro = (Button) findViewById(R.id.header_btnCadastrar);

        mHandler = new Handler();
        mDeveExibirDialog = savedInstanceState == null;
        ImageView imv = (ImageView) findViewById(R.id.imgLocal);

        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ultimaLocalizacao();
            }
        });

        //Testar
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addConnectionCallbacks(this).addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).build();
        googleApiClient.connect();
        mapFragment.getMapAsync(this);

    }

    //CONFIGURAÇÃO LAYOUT
    private void navigationMenu() {
        //navigationView.getMenu().clear();
        //navigationView.inflateHeaderView(R.layout.nav_header_principal_);
        //navigationView.inflateMenu(R.menu.activity_home2_drawer);

        if (UsuarioAtivoSingleton.getUsuario() == null) {
            navigationView.getMenu().clear();
            navigationView.inflateHeaderView(R.layout.nav_header_principal_login);
            navigationView.inflateMenu(R.menu.activity_home2_drawerlogin);

        } else {
            navigationView.getMenu().clear();
            navigationView.inflateHeaderView(R.layout.nav_header_principal_);
            navigationView.inflateMenu(R.menu.activity_home2_drawer);

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_mapearlocal) {
            ultimaLocalizacao();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction().replace(R.id.container, Register_Map_Fragmento.newInstance(1));
            ft.addToBackStack(null);
            ft.commit();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    // CONFIGURAÇÃO MAPA

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        estilizarMap();
        mMap.setMinZoomPreference(10);
        enableMyLocation();
    }

    private void estilizarMap() {
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Erro ao estilizar");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    private void ultimaLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            mTentativas = 0;
            mOrigem = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mOrigem, 15));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mOrigem, 15));
        } else if (mTentativas < 10) {
            mTentativas++;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ultimaLocalizacao();
                }
            }, 2000);
        }
        localizacao = location;
        Log.e(TAG, String.valueOf(location.getLatitude()));
        Log.e(TAG, String.valueOf(location.getLongitude()));

    }

    private boolean enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(Principal_Activity.this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setRotateGesturesEnabled(false);
            flagEnableMap = true;

        } else {
            flagEnableMap = false;
        }
        return flagEnableMap;
    }


    @Override
    public void onConnected(Bundle bundle) {
        verificarGPS();
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient.disconnect();
    }

    @Override
    public void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        mHandler.removeCallbacksAndMessages(null);
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        enableMyLocation();
    }

    private void verificarGPS() {
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder locationSenttingsRequest = new LocationSettingsRequest.Builder();
        locationSenttingsRequest.setAlwaysShow(true);
        locationSenttingsRequest.addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(
                googleApiClient, locationSenttingsRequest.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        ultimaLocalizacao();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        if (mDeveExibirDialog) {
                            try {
                                status.startResolutionForResult(Principal_Activity.this, REQUEST_CHECAR_GPS);
                                mDeveExibirDialog = false;
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.wtf("NGVL", "Isso não deveria acontecer");
                        break;
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_DIALOG, mDeveExibirDialog);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDeveExibirDialog = savedInstanceState.getBoolean(EXTRA_DIALOG, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ERRO_PLAY_SERVICES && resultCode == RESULT_OK) {
            googleApiClient.connect();
        } else if (requestCode == REQUEST_CHECAR_GPS) {
            if (resultCode == RESULT_OK) {
                mTentativas = 0;
                mHandler.removeCallbacks(null);
                ultimaLocalizacao();
            } else {
                Toast.makeText(this, "É necessário habilitar a configuração de localização para utilizar o aplicativo", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    public boolean verificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        return conectado;
    }

    public void header_btnLogin(View view) {
        Intent intent = new Intent(Principal_Activity.this, Login_Activity.class);
        intent.putExtra("chave", "login");
        startActivity(intent);
    }

    public void header_btnCadastrar(View view) {
        Intent intent = new Intent(Principal_Activity.this, Login_Activity.class);
        intent.putExtra("chave", "cadastro");
        startActivity(intent);
    }

    public void telaLogin(View view) {
        Intent intent = new Intent(Principal_Activity.this, Login_Activity.class);
        startActivity(intent);
    }
}
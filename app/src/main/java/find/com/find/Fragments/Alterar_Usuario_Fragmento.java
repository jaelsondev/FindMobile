package find.com.find.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;


import de.hdodenhof.circleimageview.CircleImageView;
import find.com.find.Activies.Principal_Activity;
import find.com.find.Model.Usuario;
import find.com.find.Model.UsuarioApplication;
import find.com.find.R;
import find.com.find.Services.FindApiAdapter;
import find.com.find.Services.FindApiService;
import find.com.find.Util.Validacoes;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaelson on 19/09/2017.
 */

public class Alterar_Usuario_Fragmento extends Fragment {

    //Constantites
    private static final int PICK_IMAGE = 123;
    private static final int CAM_IMAGE = 124;
    private Button btnFecharCard, btnFecharCardDados, btnFecharCardDesativar;

    private TextView tvNome, tvEmail, tvSenha, tvSexo, tvDesativar;
    private CardView cardView, cardViewDados, cardViewDesativar;
    private EditText senhaAtual, novaSenha, edtNome;
    private RadioButton rbFeminino;
    private RadioButton rbMasculino;
    private FloatingActionButton card_btnAlterarDados;
    private Button card_btnAlterar, btnAlterar, btnDesativarSim, btnDesativarNao;

    private ImageButton btnOpImage, btnCamera, btnGalery;
    private CircleImageView icPerfil;
    private boolean open;

    //Imagem
    private Uri imagemSelecionada;
    private Bitmap bitmap;

    public static Alterar_Usuario_Fragmento newInstance() {
        Alterar_Usuario_Fragmento fragmento = new Alterar_Usuario_Fragmento();
        return fragmento;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cardview_alterar_dados_ususario, container, false);

        //Principal
        tvNome = (TextView) view.findViewById(R.id.alterar_tvNome);
        tvEmail = (TextView) view.findViewById(R.id.alterar_tvEmail);
        tvSenha = (TextView) view.findViewById(R.id.alterar_tvSenha);
        tvDesativar = (TextView) view.findViewById(R.id.alterar_tvDesativar);
        tvSexo = (TextView) view.findViewById(R.id.alterar_tvSexo);
        //Card Senha
        cardView = (CardView) view.findViewById(R.id.card_alterarsenha);
        card_btnAlterar = (Button) view.findViewById(R.id.card_btnAlterarSenha);
        senhaAtual = (EditText) view.findViewById(R.id.senha_atual);
        novaSenha = (EditText) view.findViewById(R.id.nova_senha);
        btnFecharCard = (Button) view.findViewById(R.id.card_fechar);
        btnFecharCardDados = (Button) view.findViewById(R.id.card_fecharDados);

        //Card Dados
        cardViewDados = (CardView) view.findViewById(R.id.card_alterarDados);
        card_btnAlterarDados = (FloatingActionButton) view.findViewById(R.id.card_btnAlterarDados);
        btnAlterar = (Button) view.findViewById(R.id.alterar_btnAlterar);
        edtNome = (EditText) view.findViewById(R.id.alterar_edtNome);
        rbFeminino = (RadioButton) view.findViewById(R.id.alterar_rbFeminino);
        rbMasculino = (RadioButton) view.findViewById(R.id.alterar_rbMasculino);
        icPerfil = (CircleImageView) view.findViewById(R.id.alterar_icPerfil);

        //CardDesativar
        cardViewDesativar = (CardView) view.findViewById(R.id.card_Desativar);
        btnFecharCardDesativar = (Button) view.findViewById(R.id.card_fecharCardDesativar);
        btnDesativarNao = (Button) view.findViewById(R.id.card_btnDesativarNao);
        btnDesativarSim = (Button) view.findViewById(R.id.card_btnDesativarSim);

        //Pegar imagem
        btnOpImage = (ImageButton) view.findViewById(R.id.register_btnOpImage);
        btnCamera = (ImageButton) view.findViewById(R.id.register_btnCamera);
        btnGalery = (ImageButton) view.findViewById(R.id.register_btnGalery);
        pegarImagem();
        atribuirDadosUser();
        cardsView();
        //Fim Pegar Imagem

        //CHAMADA PARA ALTERAR A SENHA DO USUÁRIO
        card_btnAlterar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarSenha()) {
                    UsuarioApplication.getUsuario().setSenha(novaSenha.getText().toString());

                    FindApiService servicos = FindApiAdapter.createService(FindApiService.class, UsuarioApplication.getToken().getToken());
                    final Call<Usuario> call = servicos.atualizarUsuario(UsuarioApplication.getToken().getToken(), UsuarioApplication.getUsuario());
                    call.enqueue(new Callback<Usuario>() {
                        @Override
                        public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                            if (response.code() == 200) {
                                Toast.makeText(getContext(), "Senha alterada", Toast.LENGTH_SHORT).show();
                                cardView.setVisibility(View.GONE);

                            }
                        }

                        @Override
                        public void onFailure(Call<Usuario> call, Throwable t) {
                            Toast.makeText(getContext(), "Não foi possível fazer a conexão", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        //CHAMADA PARA ALTERAR OS DADOS PESSOAIS DO USUÁRIO
        btnAlterar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    UsuarioApplication.getUsuario().setNome(edtNome.getText().toString());
                    if (rbMasculino.isChecked()) {
                        UsuarioApplication.getUsuario().setSexo("Masculino");
                    } else {
                        UsuarioApplication.getUsuario().setSexo("Feminino");
                    }

                    FindApiService servicos = FindApiAdapter.createService(FindApiService.class, UsuarioApplication.getToken().getToken());
                    final Call<Usuario> call = servicos.atualizarUsuario(UsuarioApplication.getToken().getToken(), UsuarioApplication.getUsuario());
                    call.enqueue(new Callback<Usuario>() {
                        @Override
                        public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                            if (response.code() == 200) {
                                Toast.makeText(getContext(), "Dados Alterados", Toast.LENGTH_SHORT).show();
                                cardViewDados.setVisibility(View.GONE);
                                atribuirDadosUser();
                            }
                        }

                        @Override
                        public void onFailure(Call<Usuario> call, Throwable t) {
                            Toast.makeText(getContext(), "Não foi possível fazer a conexão", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        //CHAMADA PARA DESATIVAR CONTA DO USUÁRIO
        btnDesativarSim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Desatiar conta do usuário
                FindApiService servicos = FindApiAdapter.createService(FindApiService.class, UsuarioApplication.getToken().getToken());
                final Call<Void> call = servicos.desativarUsuario(UsuarioApplication.getUsuario().getIdUsuario());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        //String retorno = response.body();
                        if (response.code() == 200) {
                            Toast.makeText(getContext(), "Conta Desativada", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getContext(), Principal_Activity.class);
                            UsuarioApplication.setUsuario(null);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Não foi possível fazer a conexão", Toast.LENGTH_SHORT).show();
                    }
                });

            }

        });

        return view;
    }

    //METODO RESPONSAVEL POR CAPTURAR A IMAGEM SELECIONADA E CHAMAR O METODO PARA MUDAR NO SERVIDOR
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                imagemSelecionada = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imagemSelecionada);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                icPerfil.setImageBitmap(bitmap);
                alterarUsuarioImagem();
            }

            if (requestCode == CAM_IMAGE) {
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");
                icPerfil.setImageBitmap(bitmap);
                imagemSelecionada = Validacoes.getImageUri(getContext(), bitmap);
                alterarUsuarioImagem();
            }
        }
    }

    //METODO RESPONSÁVEL POR ALTERAR A IMAGEM DO USUÁRIO NO SERVIDOR
    private void alterarUsuarioImagem() {
        FindApiService servicos = FindApiAdapter.createService(FindApiService.class, UsuarioApplication.getToken().getToken());
        File file = new File(Validacoes.getPath(getContext(), imagemSelecionada));
        RequestBody fbody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), fbody);
        Call<ResponseBody> call = servicos.atualizarUsuarioImagem(UsuarioApplication.getUsuario().getIdUsuario(), UsuarioApplication.getUsuario().getEmail(), body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.code() == 200) {
                    Toast.makeText(getContext(), "Imagem alterada com sucesso", Toast.LENGTH_SHORT).show();
                    String url = null;
                    try {
                        url = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    UsuarioApplication.getUsuario().setUrlImgPerfil(url);
                    Validacoes.carregarImagemUser(getActivity(),icPerfil);
                    Log.i("url", UsuarioApplication.getUsuario().getUrlImgPerfil());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Falha", t.getMessage());
                Toast.makeText(getContext(), "Não foi possível fazer a conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //METODO PARA VALIDAR O PROCESSO DE ALTERAR SENHA
    private boolean validarSenha() {
        if (TextUtils.isEmpty(senhaAtual.getText().toString())) {
            senhaAtual.setError("Campo não pode ser vazio");
            return false;
        }

        if (TextUtils.isEmpty(novaSenha.getText().toString())) {
            novaSenha.setError("Campo não pode ser vazio");
            return false;
        }

        if (!UsuarioApplication.getUsuario().getSenha().equals(senhaAtual.getText().toString())) {
            senhaAtual.setError("Senha incorreta");
            return false;
        }


        return true;
    }


    //METODO PARA POPULAR OS DADOS DO USUARIO
    private void atribuirDadosUser() {
        tvNome.setText(UsuarioApplication.getUsuario().getNome());
        tvEmail.setText(UsuarioApplication.getUsuario().getEmail());
        tvSexo.setText(UsuarioApplication.getUsuario().getSexo());
        edtNome.setText(UsuarioApplication.getUsuario().getNome());

        if (UsuarioApplication.getUsuario().getSexo().equals("Masculino")) {
            rbMasculino.setChecked(true);
        } else {
            rbFeminino.setChecked(true);
        }
        if (UsuarioApplication.getUsuario().getUrlImgPerfil() != null) {
            Validacoes.carregarImagemUser(getActivity(),icPerfil);
        }
    }


    //METODO PARA VALIDAR OS CAMPOS DE ALTERAÇÃO
    private boolean validarCampos() {
        if (TextUtils.isEmpty(edtNome.getText().toString())) {
            edtNome.setError("Campo não pode ser vazio");
            return false;
        }

        return true;
    }


    //METODO RESPONSAVEL POR CARREGAR OS CARDS VIEW QUE SÃO UTILIZADOS
    private void cardsView() {
        tvSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cardView.setVisibility(View.VISIBLE);
            }
        });

        tvDesativar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardViewDesativar.setVisibility(View.VISIBLE);
            }
        });

        card_btnAlterarDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardViewDados.setVisibility(View.VISIBLE);
            }
        });


        btnFecharCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardView.setVisibility(View.GONE);
            }
        });

        btnFecharCardDesativar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardViewDesativar.setVisibility(View.GONE);
            }
        });

        btnDesativarNao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardViewDesativar.setVisibility(View.GONE);
            }
        });

        btnFecharCardDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardViewDados.setVisibility(View.GONE);
            }
        });
    }

    //METODO RESPONSAVEL POR CAPTURAR OU PEGAR A IMAGEM PARA UTILIZAR NO PERFIL DO USUÁRIO
    private void pegarImagem() {
        btnOpImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (open) {
                    btnCamera.setVisibility(View.GONE);
                    btnCamera.setClickable(false);

                    btnGalery.setVisibility(View.GONE);
                    btnGalery.setClickable(false);

                    open = false;

                } else {
                    btnCamera.setVisibility(View.VISIBLE);
                    btnCamera.setClickable(true);

                    btnGalery.setVisibility(View.VISIBLE);
                    btnGalery.setClickable(true);

                    open = true;
                }
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(intent, "Camera imagem"), CAM_IMAGE);
                }

            }
        });

        btnGalery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Selecionar imagem"), PICK_IMAGE);
            }
        });
    }
}

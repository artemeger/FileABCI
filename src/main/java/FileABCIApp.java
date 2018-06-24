import classes.Transaction;
import com.github.jtendermint.jabci.api.*;
import com.github.jtendermint.jabci.socket.ConnectionListener;
import com.github.jtendermint.jabci.socket.TSocket;
import com.github.jtendermint.jabci.types.*;
import com.google.protobuf.ByteString;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import crypto.RipeMD160;
import data.AppState;

import javax.xml.bind.DatatypeConverter;


public class FileABCIApp implements IBeginBlock, ICheckTx, ICommit, IInfo, IDeliverTx, IQuery, IEndBlock {

    private static final Logger LOG = Logger.getLogger(FileABCIApp.class.getName());
    private ByteString appState;
    private long currentHeight;

    FileABCIApp(ConnectionListener listener) throws InterruptedException{

        appState = ByteString.copyFrom(new byte[0]);
        currentHeight = 0L;

        if(AppState.exists()){
            appState = (ByteString) AppState.loadAppState().get(AppState.KEYAPP);
            currentHeight = (Long)  AppState.loadAppState().get(AppState.KEYHEIGHT);
            System.out.println(appState.toStringUtf8());
        } else AppState.saveAppState(appState, currentHeight);

        TSocket socket = new TSocket((exception, event) -> {}, listener, (name, remaining) -> {});
        socket.registerListener(this);
        try{
            Thread t = new Thread(socket::start);
            t.setName("FileABCI Socket Thread");
            t.setDaemon(true);
            t.start();
            LOG.info("Socket on: " + t.getState());
            Thread.sleep(1000L);
        } catch (IllegalStateException e) {
            LOG.log(Level.INFO, "Error in ABCI App Socket Thread: " + e.getMessage());
        }

    }
    @Override
    public ResponseBeginBlock requestBeginBlock(RequestBeginBlock requestBeginBlock) {
        LOG.log(Level.INFO, "Begin Block: " + requestBeginBlock.getHash());
        currentHeight = requestBeginBlock.getHeader().getHeight();
        if(requestBeginBlock.getHeader().getNumTxs() != 0){
            try {
                appState =  RipeMD160.getHashFromBytes(appState);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return ResponseBeginBlock.newBuilder().build();
    }

    @Override
    public ResponseCheckTx requestCheckTx(RequestCheckTx requestCheckTx) {
        LOG.log(Level.INFO, "Check tx: " + requestCheckTx.getTx());
        return ResponseCheckTx.newBuilder().setCode(CodeType.OK).build();
    }

    @Override
    public ResponseDeliverTx receivedDeliverTx(RequestDeliverTx requestDeliverTx) {
        LOG.log(Level.INFO, "Deliver tx: " + requestDeliverTx.getTx());
        ResponseDeliverTx.Builder builder = ResponseDeliverTx.newBuilder();
        byte[] base64Decoded = DatatypeConverter.parseBase64Binary(requestDeliverTx.getTx().toStringUtf8());
        Gson gson = new Gson();
        Transaction trans = gson.fromJson(new String(base64Decoded) , Transaction.class);
        if(trans.getOwner() != null) {
            KVPair.Builder kvbuilder = KVPair.newBuilder();
            kvbuilder.setKey(ByteString.copyFromUtf8("account.owner"));
            kvbuilder.setValue(ByteString.copyFromUtf8(trans.getOwner()));
            builder.setTags(0, kvbuilder.build());
        }
        return builder.setCode(CodeType.OK).build();
    }

    @Override
    public ResponseEndBlock requestEndBlock(RequestEndBlock requestEndBlock) {
        LOG.log(Level.INFO, "End Block: " + requestEndBlock.getHeight());
        return ResponseEndBlock.newBuilder().build();
    }

    @Override
    public ResponseCommit requestCommit(RequestCommit requestCommit) {
        LOG.log(Level.INFO, "Commit");
        ResponseCommit.Builder builder = ResponseCommit.newBuilder();
        try{
            builder.setData(appState);
            AppState.saveAppState(appState, currentHeight);
        } catch (Exception e) {
            LOG.log(Level.INFO, "Bad Commit: " + e.getMessage());
        }
        return builder.build();
    }

    @Override
    public ResponseInfo requestInfo(RequestInfo requestInfo) {
        LOG.log(Level.INFO, "Info: " + requestInfo.getVersion());
        ResponseInfo.Builder response = ResponseInfo.newBuilder();
        response.setLastBlockAppHash(appState);
        response.setLastBlockHeight(currentHeight);
        return response.build();
    }

    @Override
    public ResponseQuery requestQuery(RequestQuery requestQuery) {
        LOG.log(Level.INFO, "Query: " + requestQuery.getData());
        return ResponseQuery.newBuilder().build();
    }

}

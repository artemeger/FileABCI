import com.github.jtendermint.jabci.api.ABCIAPI;
import com.github.jtendermint.jabci.api.CodeType;
import com.github.jtendermint.jabci.socket.ConnectionListener;
import com.github.jtendermint.jabci.socket.TSocket;
import com.github.jtendermint.jabci.types.RequestBeginBlock;
import com.github.jtendermint.jabci.types.RequestCheckTx;
import com.github.jtendermint.jabci.types.RequestCommit;
import com.github.jtendermint.jabci.types.RequestDeliverTx;
import com.github.jtendermint.jabci.types.RequestEcho;
import com.github.jtendermint.jabci.types.RequestEndBlock;
import com.github.jtendermint.jabci.types.RequestFlush;
import com.github.jtendermint.jabci.types.RequestInfo;
import com.github.jtendermint.jabci.types.RequestInitChain;
import com.github.jtendermint.jabci.types.RequestQuery;
import com.github.jtendermint.jabci.types.RequestSetOption;
import com.github.jtendermint.jabci.types.ResponseBeginBlock;
import com.github.jtendermint.jabci.types.ResponseCheckTx;
import com.github.jtendermint.jabci.types.ResponseCommit;
import com.github.jtendermint.jabci.types.ResponseDeliverTx;
import com.github.jtendermint.jabci.types.ResponseEcho;
import com.github.jtendermint.jabci.types.ResponseEndBlock;
import com.github.jtendermint.jabci.types.ResponseFlush;
import com.github.jtendermint.jabci.types.ResponseInfo;
import com.github.jtendermint.jabci.types.ResponseInitChain;
import com.github.jtendermint.jabci.types.ResponseQuery;
import com.github.jtendermint.jabci.types.ResponseSetOption;
import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


import crypto.RipeMD160;
import data.AppState;


public class FileABCIApp implements ABCIAPI {

    private static final Logger LOG = Logger.getLogger(FileABCIApp.class.getName());
    private ByteString appState;
    private long currentHeight;

    FileABCIApp(ConnectionListener listener) throws InterruptedException{

        appState = ByteString.copyFrom(new byte[0]);
        currentHeight = 0L;

        if(AppState.exists()){
            HashMap<String, Object> nodeState;
            nodeState = AppState.loadAppState();
            appState = (ByteString) nodeState.get(AppState.KEYAPP);
            LOG.log(Level.INFO, "APPP: " + appState);
            currentHeight = (Long) nodeState.get(AppState.KEYHEIGHT);
            LOG.log(Level.INFO, "HEIGHT: " + currentHeight);
        } else {
            updateAppState(appState);
        }

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
        appState = loadAppState();
        updateHeight(requestBeginBlock.getHeader().getHeight());
        return ResponseBeginBlock.newBuilder().build();
    }

    @Override
    public ResponseCheckTx requestCheckTx(RequestCheckTx requestCheckTx) {
        LOG.log(Level.INFO, "Check tx: " + requestCheckTx.getTx());
        return ResponseCheckTx.newBuilder().setCode(CodeType.OK).build();
    }

    @Override
    public ResponseCommit requestCommit(RequestCommit requestCommit) {
        LOG.log(Level.INFO, "Commit");
        LOG.log(Level.INFO, "NOW APP:"+appState.toStringUtf8());
        ResponseCommit.Builder builder = ResponseCommit.newBuilder();
        try{
            builder.setData(RipeMD160.getHashFromBytes(appState));
            updateAppState(RipeMD160.getHashFromBytes(appState));
            LOG.log(Level.INFO, "THEN APP:"+appState.toStringUtf8());
        } catch (Exception e) {
            LOG.log(Level.INFO, "Bad Commit: " + e.getMessage());
        }
        return builder.build();
    }

    @Override
    public ResponseDeliverTx receivedDeliverTx(RequestDeliverTx requestDeliverTx) {
        LOG.log(Level.INFO, "Deliver tx: " + requestDeliverTx.getTx());
        return ResponseDeliverTx.newBuilder().setCode(CodeType.OK).build();
    }

    @Override
    public ResponseEcho requestEcho(RequestEcho requestEcho) {
        LOG.log(Level.INFO, "Echo: " + requestEcho.getMessage());
        return ResponseEcho.newBuilder().setMessage(requestEcho.getMessage()).build();
    }

    @Override
    public ResponseEndBlock requestEndBlock(RequestEndBlock requestEndBlock) {
        LOG.log(Level.INFO, "End Block: " + requestEndBlock.getHeight());
        return ResponseEndBlock.newBuilder().build();
    }

    @Override
    public ResponseFlush requestFlush(RequestFlush requestFlush) {
        LOG.log(Level.INFO, "Flush");
        return ResponseFlush.newBuilder().build();
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
    public ResponseInitChain requestInitChain(RequestInitChain requestInitChain) {
        LOG.log(Level.INFO, "Init Chain");
        return ResponseInitChain.newBuilder().build();
    }

    @Override
    public ResponseQuery requestQuery(RequestQuery requestQuery) {
        LOG.log(Level.INFO, "Query: " + requestQuery.getData());
        return ResponseQuery.newBuilder().build();
    }

    @Override
    public ResponseSetOption requestSetOption(RequestSetOption requestSetOption) {
        LOG.log(Level.INFO, "Set Option: " + requestSetOption.getKey());
        return ResponseSetOption.newBuilder().build();
    }

    private void updateAppState(ByteString newState){
        appState = newState;
        AppState.saveAppState(appState, currentHeight);
    }

    private void updateHeight(long height){
        currentHeight = height;
        AppState.saveAppState(appState, currentHeight);
    }
    private ByteString loadAppState(){
        return (ByteString) AppState.loadAppState().get(AppState.KEYAPP);
    }
}

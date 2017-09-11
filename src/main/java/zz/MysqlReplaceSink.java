package zz;

import com.google.common.collect.Lists;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by guojiaozhen on 2017/8/28.
 */
public class MysqlReplaceSink extends AbstractSink implements Configurable {

    private Logger LOG = LoggerFactory.getLogger(MysqlReplaceSink.class);

    private String hostname;
    private String port;
    private String databaseName;
    private String tableName;
    private String fields;
    private String user;
    private String password;
    private int batchSize;

    @Override
    public Status process() throws EventDeliveryException {
        Status status = Status.READY;

        Channel channel = getChannel();
        Transaction transaction = null;

        List<String> actions = Lists.newArrayList();

        try {
            transaction = channel.getTransaction();
            transaction.begin();
            for (int i = 0; i < batchSize; i++) {
                Event event = channel.take();
                if (null != event) {
                    //此处写你的业务逻辑 start
                    String context = new String(event.getBody());
                    LOG.info("内容是" + context);
                    String sql = MysqlSinkUtil.getSqlReplace(context, databaseName + "." + tableName, fields);
                    LOG.info("sql是：" + sql);
                    actions.add(sql);
                    //此处写你的业务逻辑 end
                } else {
                    status = Status.BACKOFF;
                }
            }
            MysqlSinkUtil.toMysql(hostname, port, databaseName, user, password, actions);
            transaction.commit();

        } catch (Exception e) {
            LOG.error("数据异常，丢弃这个event");
            if (null != transaction) {
                transaction.commit();
            }
            throw new EventDeliveryException(e);
        } finally {
            if (null != transaction) {
                transaction.close();
            }
        }
        return status;
    }

    @Override
    public void configure(Context context) {
        hostname = context.getString("hostname");
        port = context.getString("port");
        databaseName = context.getString("databaseName");
        tableName = context.getString("tableName");
        fields = context.getString("fields");
        user = context.getString("user");
        password = context.getString("password");
        batchSize = context.getInteger("batchSize", 100);
    }


}

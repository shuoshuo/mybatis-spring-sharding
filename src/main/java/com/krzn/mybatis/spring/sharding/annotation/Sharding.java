package com.krzn.mybatis.spring.sharding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.krzn.mybatis.spring.sharding.RW;
import com.krzn.mybatis.spring.sharding.ShardingValueStrategy;
import com.krzn.mybatis.spring.sharding.support.SimpleShardingValueStrategy;

/**
 * 用于支持分库分表、读写分离的注解。
 * <p>这个注解用在Dao的方法上，注解中需要指定分表前缀、
 * 分表属性、拆分策略、读写标志等。
 *
 * <h2>tablePrefix：</h2>
 * <p>分表前缀指的是在sql中出现的(分)表名，这个是虚拟表名(或者逻辑表名)，
 * <p>比如：分表sql语句 "select * from user" 中表名是user，
 * 但实际的表名可能是user001。所以这里称之为<u>分表前缀</u>。
 * <p><b>注意：注解中的tablePrefix要和sql中的表名保持一致!</b>
 *
 * <h2>property：</h2>
 * <p>分表属性指的是当前注解修饰的参数中表示分表字段值得参数"名称"。
 * <li>例1：方法参数列表[id,name]，id由Mybatis的@param("userId")修饰，
 * 实际的数据库分表字段是'_id'，对应方法中传入的id参数。
 * <u>那么property的值就是userId。</u>
 * <li>例2：方法参数列表[user]，user是一个Vo，包含属性userId、name，
 * 实际的数据库分表字段是'_id'，对应方法中传入的person参数的userId属性。
 * <u>那么property的值就是userId。</u>
 * <p>
 * <p><b>可见property不等同于数据库分表字段的名称，它的作用是从方法参数
 * 列表中找到分表字段对应的值。</b>
 *
 * <h2>shardingValueStrategy：</h2>
 * <p>这个属性是指拆分(分库分表)值的计算策略，容易和拆分策略(分表规则)混淆。
 * <p>举个例子：使用userId的后6位进行分库分表，分表规则是计算hashcode，
 * 然后对表总数取模。
 * <p>那么整个过程中包含2步：
 * <li>1.对userId截取后6位。这个过程会使用shardingValueStrategy。
 * <li>2.对第1步得到的结果进行哈希，取模。
 * <p>
 * <p>使用方可以根据业务规则自定义shardingValueStrategy。
 * <p>比如：查询卖家订单方法，卖家维度是按照卖家ID的后4位进行分表。
 * <p>那么可以定义如下策略：
 * <pre>
 * <code>
 *     public class SellerOrderShardingValueStrategy implements ShardingValueStrategy{
 *
 *         public Object get(Object propertyValue) {
 *             String sellerId = propertyValue.toString();
 *             return sellerId.substring(id.length() - 4);
 *         }
 *     }
 *
 *     public class OrderDao{
 *
 *         <code>@Sharding
 *                  (tablePrefix = "order",
 *                      property = "id",
 *         shardingValueStrategy = SellerOrderShardingValueStrategy.class
 *                            rw = RW.READ, )
 *         public List getOrdersBySellerId(@Param("id") String SellerId){
 *             ...
 *         }
 *
 *         ...
 *
 *     }
 * <code>
 * </pre>
 * <p>如果注解中未指定，会默认使用这个实现-
 * {@link SimpleShardingValueStrategy}
 *
 *
 * <h2>rw：</h2>
 * <p>读写分离标示，写操作会命中主库，读操作会命中从库。
 *
 * @author yangzhishuo
 * @version 1.0
 * @date 2016年3月18日
 * @see ShardingValueStrategy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Sharding {

    /**
     * 分表的表前缀，sql中一般直接将这个前缀作为逻辑表名。
     */
    String tablePrefix() default "";

    /**
     * 分表的属性名称。
     * <p>注意不是数据库字段。
     */
    String property();

    /**
     * sharding值计算策略。
     */
    Class<? extends ShardingValueStrategy> shardingValueStrategy() default SimpleShardingValueStrategy.class;

    /**
     * 读写标示。
     */
    RW rw() default RW.NS;

}

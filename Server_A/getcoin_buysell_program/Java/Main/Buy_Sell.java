package Main;

import org.json.JSONObject;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Buy_Sell implements Buy_Sell_func, Runnable {
//사고파는 방식을 교체할 수 있도록 설정

    private Buy_Sell_func method = null;
    private Userdata userdata = new Userdata();
    private MySQL mysql = new MySQL();
    private String coinname;
    private double percent;

    public void run() {

        System.out.println("BUY SELL START");

        /*
        Server A에서 해야할 일은
        먼저 데이터를 10개를 가져옴, 이후 10개의 데이터를 모두 처리하고, 깔끔한 +/- % 형으로 정리하는 것
        정리한 다음 traindata/avaliable.txt 에다가 정보를 줬다고 알림 (1로 변경)

        정보를 받아올 때까지 기다림.
        result/avaliable.txt 가 1이면
        값을 읽어온 후 avaliable.txt를 0으로 변경
        mysql에 출력 data 저장
        result들 삭제
        받아온 값을 이용해 살 코인과 예상 퍼센트 결정




         */
        get_trainData();
        wait_train_response();
        read_from_response();

        try {
            Runtime.getRuntime().exec("rm -rf /home/lutergs_server/NFS/result/" /*저장 경로 */);
        } catch (IOException e) {
            e.printStackTrace();
        }


        mysql.insert_resultData(coinname, percent);

        //만약 코인의 상승률이 없을 경우에는 몇분간 존버시키자. Thread.sleep 으로 한시간정도 기다리게
        //코인 구매후 기다림을 하나의 함수로!



        //TEST, 마저 작성해야 함
    }

    public JSONObject buy_coin() {
        return null;
    }

    public JSONObject sell_coin() {
        return null;
    }


    private void get_trainData() {


        int latest = this.userdata.get_latestnum("./LATEST_DATA.txt");
        double[][] coindata_num;

        ResultSet requested = this.mysql.get_latest_traindata(latest);

        //코인 순서는
        //eos, bch, qtum, iota, ltc, etc, btg, btc, omg, eth, xrp
        write_data(set_mysql_data(read_from_mysql(requested)));
        this.userdata.set_latestnum(1, "/home/lutergs_server/NFS/traindata/avaliable.txt");

        System.out.println("GET_DATA finished");
    }

    private String[][] read_from_mysql(ResultSet requested){

        String[][] coinData_string = new String[11][21];
        int count = 0;

        try {
            while (requested.next()) {
                coinData_string[0][count] = requested.getString("eos");
                coinData_string[1][count] = requested.getString("bch");
                coinData_string[2][count] = requested.getString("qtum");
                coinData_string[3][count] = requested.getString("iota");
                coinData_string[4][count] = requested.getString("ltc");
                coinData_string[5][count] = requested.getString("etc");
                coinData_string[6][count] = requested.getString("btg");
                coinData_string[7][count] = requested.getString("btc");
                coinData_string[8][count] = requested.getString("omg");
                coinData_string[9][count] = requested.getString("eth");
                coinData_string[10][count] = requested.getString("xrp");
                count++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return coinData_string;
    }

    private double[][] set_mysql_data(String[][] input){

        double[][] output = new double[11][20];
        double temp1, temp2;
        int a, b;

        for(a = 0; a < 11; a++){
            for(b = 0; b < 20; b++){
                temp1 = Double.parseDouble(input[a][b]);
                temp2 = Double.parseDouble(input[a][b + 1]);
                output[a][b] = (temp1 - temp2) / temp2 * 100;
            }
        }

        return output;
    }

    private void write_data(double[][] input){

        File file_output = new File("/home/lutergs_server/NFS/traindata/TRAIN_DATA.txt");
        BufferedWriter writer;
        int a, b;

        try {
            writer = new BufferedWriter(new FileWriter(file_output));
            for(a = 0; a < 11; a++){
                for(b = 0; b < 20; b++){
                    writer.write(String.valueOf(input[a][b]));
                    writer.write(",");
                }
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void wait_train_response(){

        File file;
        int temp;
        while(true){
            file = new File("/home/lutergs_server/NFS/result/avaliable.txt");
            temp = this.userdata.get_latestnum("/home/lutergs_server/NFS/result/avaliable.txt");
            if(temp == 1) {
                break;
            }
        }
    }


    private void read_from_response(){

        File file = new File("/home/lutergs_server/NFS/result/" /* 저장 경로 */);
        try {
            Scanner scan = new Scanner(file);
            this.coinname = scan.nextLine();
            this.percent = Double.parseDouble(scan.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        userdata.set_latestnum(0, "/home/lutergs_server/NFS/result/avaliable.txt");



    }
}
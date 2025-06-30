package controller;

import dal.LopHocPhanDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import model.LopHocPhan;
import model.TaiKhoan;

@WebServlet(name = "QuanLiLopHocPhanController", urlPatterns = { "/quanlilophocphan" })
public class QuanLiLopHocPhanController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        TaiKhoan taiKhoan = (TaiKhoan) session.getAttribute("SessionLogin");

        if (taiKhoan == null || !"3".equals(taiKhoan.getQuyen())) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            LopHocPhanDao lhpDao = new LopHocPhanDao();
            String action = request.getParameter("action");

            if ("edit".equals(action)) {
                String maLHP = request.getParameter("id");
                LopHocPhan lhp = lhpDao.getLopHocPhanById(maLHP);
                if (lhp != null) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    // Convert LopHocPhan to JSON
                    String json = String.format(
                            "{\"maLHP\":\"%s\",\"tenHP\":\"%s\",\"tenGV\":\"%s\",\"maMH\":\"%s\","
                                    + "\"hocKy\":\"%s\",\"namHoc\":\"%s\",\"soLuongSVTD\":%d,"
                                    + "\"thoiGianHoc\":\"%s\",\"phongHoc\":\"%s\",\"mocDK\":\"%s\"}",
                            lhp.getMaLHP(), lhp.getTenHP(), lhp.getTenGV(), lhp.getMaMH(),
                            lhp.getHocKy(), lhp.getNamHoc(), lhp.getSoLuongSVTD(),
                            lhp.getThoiGianHoc(), lhp.getPhongHoc(),
                            new SimpleDateFormat("yyyy-MM-dd").format(lhp.getMocDK()));
                    response.getWriter().write(json);
                    return;
                }
            } else if ("delete".equals(action)) {
                String maLHP = request.getParameter("id");
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                try {
                    LopHocPhanDao dao = new LopHocPhanDao();
                    boolean success = dao.deleteLopHocPhan(maLHP);

                    String jsonResponse;
                    if (success) {
                        jsonResponse = "{\"success\": true}";
                    } else {
                        jsonResponse = "{\"success\": false, \"message\": \"Không thể xóa lớp học phần\"}";
                    }
                    response.getWriter().write(jsonResponse);
                } catch (SQLException e) {
                    String errorJson = "{\"success\": false, \"message\": \"Lỗi khi xóa: " +
                            e.getMessage().replace("\"", "'") + "\"}";
                    response.getWriter().write(errorJson);
                }
                return;
            } else {
                String searchTerm = request.getParameter("search");
                List<LopHocPhan> listLHP;
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    listLHP = lhpDao.searchLopHocPhan(searchTerm);
                } else {
                    listLHP = lhpDao.getAllLopHocPhan();
                }

                request.setAttribute("listLHP", listLHP);
                request.getRequestDispatcher("QuanLiLopHocPhan.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("error.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if ("update".equals(action)) {
            try {
                LopHocPhan lhp = new LopHocPhan();
                lhp.setMaLHP(request.getParameter("maLHP"));
                lhp.setTenHP(request.getParameter("tenHP"));
                lhp.setTenGV(request.getParameter("tenGV"));
                lhp.setMaMH(request.getParameter("maMH"));
                lhp.setHocKy(request.getParameter("hocKy"));
                lhp.setNamHoc(request.getParameter("namHoc"));
                lhp.setSoLuongSVTD(Integer.parseInt(request.getParameter("soLuongSVTD")));
                lhp.setThoiGianHoc(request.getParameter("thoiGianHoc"));
                lhp.setPhongHoc(request.getParameter("phongHoc"));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date mocDK = sdf.parse(request.getParameter("mocDK"));
                lhp.setMocDK(mocDK);

                LopHocPhanDao dao = new LopHocPhanDao();
                if (dao.updateLopHocPhan(lhp)) {
                    response.sendRedirect("quanlilophocphan");
                } else {
                    request.setAttribute("error", "Cập nhật thất bại");
                    request.setAttribute("lhp", lhp);
                    request.setAttribute("editMode", true);
                    request.getRequestDispatcher("QuanLiLopHocPhan.jsp").forward(request, response);
                }
            } catch (SQLException | ParseException e) {
                e.printStackTrace();

            }
        } else if ("add".equals(action)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            try {
                // Validate required fields
                String maLHP = request.getParameter("maLHP");
                String tenHP = request.getParameter("tenHP");
                String tenGV = request.getParameter("tenGV");
                String maMH = request.getParameter("maMH");
                String hocKy = request.getParameter("hocKy");
                String namHoc = request.getParameter("namHoc");
                String soLuongSVTDStr = request.getParameter("soLuongSVTD");
                String thoiGianHoc = request.getParameter("thoiGianHoc");
                String phongHoc = request.getParameter("phongHoc");
                String mocDKStr = request.getParameter("mocDK");

                // Validate all required fields
                if (maLHP == null || maLHP.trim().isEmpty()) {
                    throw new IllegalArgumentException("Mã lớp học phần không được để trống");
                }
                if (tenHP == null || tenHP.trim().isEmpty()) {
                    throw new IllegalArgumentException("Tên học phần không được để trống");
                }
                if (tenGV == null || tenGV.trim().isEmpty()) {
                    throw new IllegalArgumentException("Tên giảng viên không được để trống");
                }
                if (maMH == null || maMH.trim().isEmpty()) {
                    throw new IllegalArgumentException("Mã môn học không được để trống");
                }

                LopHocPhan lhp = new LopHocPhan();
                lhp.setMaLHP(maLHP);
                lhp.setTenHP(tenHP);
                lhp.setTenGV(tenGV);
                lhp.setMaMH(maMH);
                lhp.setHocKy(hocKy);
                lhp.setNamHoc(namHoc);

                try {
                    lhp.setSoLuongSVTD(Integer.parseInt(soLuongSVTDStr));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Số lượng sinh viên tối đa không hợp lệ");
                }

                lhp.setThoiGianHoc(thoiGianHoc);
                lhp.setPhongHoc(phongHoc);

                if (mocDKStr != null && !mocDKStr.trim().isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date mocDK = sdf.parse(mocDKStr);
                        lhp.setMocDK(mocDK);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Ngày mốc đăng ký không hợp lệ");
                    }
                }

                LopHocPhanDao dao = new LopHocPhanDao();
                if (dao.createLopHocPhan(lhp)) {
                    response.getWriter().write("{\"success\": true, \"message\": \"Thêm lớp học phần thành công\"}");
                } else {
                    throw new SQLException("Không thể thêm lớp học phần");
                }
            } catch (Exception e) {
                response.getWriter()
                        .write("{\"success\": false, \"message\": \"" + e.getMessage().replace("\"", "'") + "\"}");
            }
            return;
        }
    }
}
// done

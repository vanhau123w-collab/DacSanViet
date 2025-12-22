# 🎨 CKEditor 5 - Rich Text Editor

## ✅ Đã Tích Hợp Thành Công!

CKEditor 5 đã được tích hợp vào form tạo và chỉnh sửa sản phẩm.

## 🌟 Tính Năng CKEditor 5

### Định Dạng Văn Bản
- ✅ **Headings** - H1, H2, H3 cho tiêu đề
- ✅ **Bold, Italic, Underline, Strikethrough**
- ✅ **Font Size** - Tiny, Small, Default, Big, Huge
- ✅ **Font Color** - Màu chữ
- ✅ **Background Color** - Màu nền

### Danh Sách & Căn Chỉnh
- ✅ **Bulleted List** - Danh sách dấu đầu dòng
- ✅ **Numbered List** - Danh sách đánh số
- ✅ **Alignment** - Left, Center, Right, Justify

### Nội Dung Đa Phương Tiện
- ✅ **Links** - Chèn liên kết
- ✅ **Images** - Upload và chèn hình ảnh
- ✅ **Tables** - Tạo bảng với merge cells
- ✅ **Block Quote** - Trích dẫn

### Khác
- ✅ **Undo/Redo** - Hoàn tác/Làm lại
- ✅ **Dark Theme** - Giao diện tối đẹp mắt

## 🆚 So Sánh với TinyMCE

| Tính Năng | CKEditor 5 | TinyMCE |
|-----------|------------|---------|
| **Miễn phí** | ✅ 100% | ⚠️ Cần API key |
| **Modern UI** | ✅ Rất đẹp | ✅ Đẹp |
| **Dễ customize** | ✅ Modular | ⚠️ Phức tạp |
| **Performance** | ✅ Nhanh | ✅ Nhanh |
| **Tables** | ✅ Tốt | ✅ Rất tốt |
| **Image Upload** | ✅ Có | ✅ Có |
| **Plugins** | ✅ Nhiều | ✅ Rất nhiều |
| **Community** | ✅ Lớn | ✅ Rất lớn |

## 💡 Ưu Điểm CKEditor 5

1. **Hoàn toàn miễn phí** - Không cần API key
2. **Modern & Clean** - UI/UX đẹp, hiện đại
3. **Modular** - Chỉ load những gì cần
4. **Lightweight** - Nhẹ hơn TinyMCE
5. **Active Development** - Cập nhật thường xuyên
6. **Great Documentation** - Tài liệu rõ ràng

## 🚀 Cách Sử Dụng

### Trong Form Tạo Sản Phẩm
1. Vào `/admin/products/create`
2. Nhập thông tin sản phẩm
3. Sử dụng editor để viết:
   - **Mô tả sản phẩm** - Tab đầu tiên
   - **Câu chuyện sản phẩm** - Tab thứ hai
4. Click "Tạo sản phẩm"

### Trong Form Chỉnh Sửa
1. Vào `/admin/products/edit/{id}`
2. Nội dung cũ sẽ tự động load vào editor
3. Chỉnh sửa nội dung
4. Click "Cập nhật sản phẩm"

## 🎯 Toolbar Buttons

```
Heading | Bold Italic Underline Strike | 
Font Size Font Color BG Color | 
Link Image Table Quote | 
Bullet List Number List | 
Alignment | Undo Redo
```

## 🔧 Cấu Hình

Editor được cấu hình trong:
- `create.html` - Form tạo mới
- `edit.html` - Form chỉnh sửa

### Tùy Chỉnh Toolbar

Nếu muốn thêm/bớt buttons, chỉnh sửa phần `toolbar.items`:

```javascript
toolbar: {
    items: [
        'heading', '|',
        'bold', 'italic', 'underline', 'strikethrough', '|',
        'fontSize', 'fontColor', 'fontBackgroundColor', '|',
        'link', 'uploadImage', 'insertTable', 'blockQuote', '|',
        'bulletedList', 'numberedList', '|',
        'alignment', '|',
        'undo', 'redo'
    ]
}
```

### Thêm Plugin Mới

CKEditor 5 có nhiều plugin:
- Code Block
- Horizontal Line
- Special Characters
- Emoji
- Math Equations
- Export to PDF/Word

Xem thêm: https://ckeditor.com/docs/ckeditor5/latest/features/

## 📦 CDN

Đang dùng CDN version 41.0.0:
```html
<script src="https://cdn.ckeditor.com/ckeditor5/41.0.0/classic/ckeditor.js"></script>
```

## 🎨 Dark Theme

Editor đã được style để match với admin dashboard:
- Background: Dark
- Text: Light
- Toolbar: Semi-transparent
- Hover: Primary color accent

## 🐛 Troubleshooting

### Editor không hiển thị
- Kiểm tra console có lỗi không
- Đảm bảo CDN load thành công
- Kiểm tra textarea có đúng ID không

### Nội dung không save
- Editor tự động sync với textarea
- Form submit sẽ gửi HTML content
- Backend nhận HTML và lưu vào database

### Muốn thêm tính năng
- Xem docs: https://ckeditor.com/docs/ckeditor5/
- Thêm plugin vào config
- Rebuild nếu cần

## 🌐 Tài Nguyên

- **Official Docs**: https://ckeditor.com/docs/ckeditor5/
- **Demo**: https://ckeditor.com/ckeditor-5/demo/
- **GitHub**: https://github.com/ckeditor/ckeditor5
- **Community**: https://github.com/ckeditor/ckeditor5/discussions

## 🎉 Kết Luận

CKEditor 5 là lựa chọn tuyệt vời cho DacSanViet:
- Miễn phí 100%
- Modern & Beautiful
- Đầy đủ tính năng
- Dễ sử dụng
- Active community

Enjoy! 🚀

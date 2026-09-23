<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-links">
        <div class="link-group">
          <h4>关于我们</h4>
          <ul>
            <li><a href="#">平台介绍</a></li>
            <li><a href="#">联系我们</a></li>
            <li><a href="#">加入我们</a></li>
          </ul>
        </div>
        <div class="link-group">
          <h4>帮助中心</h4>
          <ul>
            <li><a href="#">新手指南</a></li>
            <li><a href="#">交易规则</a></li>
            <li><a href="#">常见问题</a></li>
          </ul>
        </div>
        <div class="link-group">
          <h4>服务保障</h4>
          <ul>
            <li><a href="#">正品保障</a></li>
            <li><a href="#">售后服务</a></li>
            <li><a href="#">隐私保护</a></li>
          </ul>
        </div>
        <div class="link-group contact">
          <h4>联系客服</h4>
          <p>工作时间：9:00 - 22:00</p>
          <p>客服热线：400-XXX-XXXX</p>
          <p>在线客服：点击咨询</p>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© 2024 校园二手交易平台 版权所有</p>
        <p>技术支持：Vue 3 + TypeScript + Element Plus</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: #2c3e50;
  color: #ecf0f1;
  padding: 40px 0 20px;
  margin-top: auto;
}
.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}
.footer-links {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  margin-bottom: 30px;
}
.link-group h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #fff;
}
.link-group ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.link-group li {
  margin-bottom: 10px;
}
.link-group a {
  color: #bdc3c7;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.2s;
}
.link-group a:hover {
  color: #409eff;
}
.contact p {
  font-size: 13px;
  color: #bdc3c7;
  margin: 8px 0;
}
.footer-bottom {
  border-top: 1px solid #34495e;
  padding-top: 20px;
  text-align: center;
  font-size: 12px;
  color: #7f8c8d;
}
.footer-bottom p {
  margin: 4px 0;
}

@media (max-width: 768px) {
  .footer-links {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
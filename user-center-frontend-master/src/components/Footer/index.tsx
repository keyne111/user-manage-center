import {GithubOutlined} from '@ant-design/icons';
import {DefaultFooter} from '@ant-design/pro-layout';
import {PLANET_LINK} from "@/constants";

const Footer: React.FC = () => {
  const defaultMessage = '小凡出品';
  const currentYear = new Date().getFullYear();
  return (
    <DefaultFooter
      copyright={`${currentYear} ${defaultMessage}`}
      links={[
        {
          key: 'planet',
          title: '知识星球',
          href: PLANET_LINK,
          blankTarget: true,
        },
        {
          key: 'codeNav',
          title: '粤ICP备2024285775号',
          href: 'https://beian.miit.gov.cn',
          blankTarget: true,
        },
        {
          key: 'github',
          title: <><GithubOutlined/> 小凡 GitHub</>,
          href: 'https://github.com/keyne111',
          blankTarget: true,
        },

      ]}
    />
  );
};

export default Footer;

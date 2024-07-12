import { GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import React from 'react';
import {GIT_HUB} from "@/constants";

const Footer: React.FC = () => {
  return (
    <DefaultFooter
      style={{
        background: 'none',
      }}
      links={[
        {
          key: 'Ant Design Pro',
          title: '知识星球',
          href: 'https://pro.ant.design',
          blankTarget: true,
        },
        {
          key: 'Ant Design',
          title: '编程导航',
          href: 'https://ant.design',
          blankTarget: true,
        },
        {
          key: 'github',
          title: <><GithubOutlined /> 小凡 Github</>,
          href: GIT_HUB,
          blankTarget: true,
        },

      ]}
    />
  );
};

export default Footer;
